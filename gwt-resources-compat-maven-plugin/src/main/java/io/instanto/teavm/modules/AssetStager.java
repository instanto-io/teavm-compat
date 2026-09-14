package io.instanto.teavm.modules;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipFile;

/** Asset discovery is independent of widget coordinates and browser runtime code. */
final class AssetStager {
  static final String DESCRIPTOR = "META-INF/teavm-assets.properties";

  private AssetStager() {}

  static int stage(List<Path> dependencies, Path output) throws IOException {
    Map<String, Asset> assets = new TreeMap<>();
    for (Path inputDependency : dependencies) {
      Path dependency = inputDependency.toRealPath();
      if (Files.isDirectory(dependency)) {
        Path descriptor = dependency.resolve(DESCRIPTOR);
        if (!Files.isRegularFile(descriptor)) continue;
        Properties properties;
        try (InputStream input = Files.newInputStream(descriptor)) {
          properties = read(input);
        }
        String source = relative(properties.getProperty("source"));
        String target = relative(properties.getProperty("target"));
        Path root = dependency.resolve(source);
        rejectSymlinks(root);
        if (!Files.isDirectory(root)) throw new IOException("Missing asset directory " + root);
        int count = 0;
        try (var files = Files.walk(root)) {
          for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
            rejectSymlinks(file);
            String name = root.relativize(file).toString().replace(java.io.File.separatorChar, '/');
            add(assets, target + "/" + relative(name), new Asset(file, null));
            count++;
          }
        }
        if (count == 0) throw new IOException("No assets under " + root);
      } else if (dependency.toString().endsWith(".jar")) {
        try (ZipFile jar = new ZipFile(dependency.toFile())) {
          var descriptor = jar.getEntry(DESCRIPTOR);
          if (descriptor == null) continue;
          Properties properties;
          try (InputStream input = jar.getInputStream(descriptor)) {
            properties = read(input);
          }
          String source = relative(properties.getProperty("source")) + "/";
          String target = relative(properties.getProperty("target"));
          int count = 0;
          for (var entry : jar.stream().filter(e -> !e.isDirectory()).toList()) {
            if (!entry.getName().startsWith(source)) continue;
            String name = relative(entry.getName().substring(source.length()));
            add(assets, target + "/" + name, new Asset(dependency, entry.getName()));
            count++;
          }
          if (count == 0) throw new IOException("No assets under " + source + " in " + dependency);
        }
      }
    }
    if (assets.isEmpty()) {
      throw new IOException(
          "No "
              + DESCRIPTOR
              + " assets found in runtime dependencies; use widget JARs containing asset descriptors");
    }
    Path root = output.toFile().getCanonicalFile().toPath();
    // Validate every destination before modifying the served tree.
    for (String name : assets.keySet()) {
      Path destination = root.resolve(name);
      rejectSymlinks(destination);
      if (Files.isDirectory(destination))
        throw new IOException("Asset destination is a directory: " + destination);
      for (Path parent = destination.getParent(); parent != null; parent = parent.getParent()) {
        if (Files.exists(parent) && !Files.isDirectory(parent))
          throw new IOException("Asset parent is not a directory: " + parent);
      }
      if (assets.keySet().stream().anyMatch(other -> name.startsWith(other + "/")))
        throw new IOException("Conflicting asset file and directory: " + name);
    }
    for (var entry : assets.entrySet()) {
      Path destination = root.resolve(entry.getKey());
      Files.createDirectories(destination.getParent());
      entry.getValue().copyTo(destination);
    }
    return assets.size();
  }

  private static Properties read(InputStream input) throws IOException {
    Properties properties = new Properties();
    properties.load(input);
    return properties;
  }

  private static String relative(String value) throws IOException {
    if (value == null
        || value.isBlank()
        || value.startsWith("/")
        || value.contains("\\")
        || value.contains(":"))
      throw new IOException("Asset paths must be non-empty relative paths: " + value);
    String path = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    for (String part : path.split("/", -1)) {
      if (part.isEmpty() || part.equals(".") || part.equals(".."))
        throw new IOException("Invalid asset path: " + value);
    }
    return path;
  }

  private static void rejectSymlinks(Path path) throws IOException {
    for (Path current = path.toAbsolutePath(); current != null; current = current.getParent()) {
      if (Files.isSymbolicLink(current))
        throw new IOException("Asset path contains a symbolic link: " + current);
    }
  }

  private static void add(Map<String, Asset> assets, String name, Asset asset) throws IOException {
    Asset previous = assets.putIfAbsent(name, asset);
    if (previous != null && !previous.digest().equals(asset.digest()))
      throw new IOException(
          "Conflicting dependency assets for "
              + name
              + ": "
              + previous.file
              + " and "
              + asset.file);
  }

  private record Asset(Path file, String entry) {
    String digest() throws IOException {
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        if (entry == null) {
          try (InputStream input = new DigestInputStream(Files.newInputStream(file), digest)) {
            input.transferTo(OutputStream.nullOutputStream());
          }
        } else {
          try (ZipFile jar = new ZipFile(file.toFile());
              InputStream input =
                  new DigestInputStream(jar.getInputStream(jar.getEntry(entry)), digest)) {
            input.transferTo(OutputStream.nullOutputStream());
          }
        }
        return HexFormat.of().formatHex(digest.digest());
      } catch (NoSuchAlgorithmException impossible) {
        throw new IllegalStateException(impossible);
      }
    }

    void copyTo(Path destination) throws IOException {
      if (entry == null) {
        Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
      } else {
        try (ZipFile jar = new ZipFile(file.toFile());
            InputStream input = jar.getInputStream(jar.getEntry(entry))) {
          Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }
}
