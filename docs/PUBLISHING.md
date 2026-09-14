# Transferring refined source to Instanto

This procedure transfers tested source from private development into Instanto-io.
Versioned Maven releases are a separate step, run from the Instanto-io checkout
using the common `instanto-org-pom` settings and `instanto-poms/RELEASING.md`.
Create the version branch there, publish from it, then advance Instanto main to
the next snapshot. Preserve all published release branches during later source
transfers, and carry the next snapshot version back into private development.

Development history belongs in the private `cstainton/teavm-compat` repository.
In the working checkout, `origin` points there and `release` points to the release
`instanto-io/teavm-compat` repository.

Build and test locally, then commit and push the complete working history.
Publish the tested file tree to Instanto as a single parentless commit, created
with `git commit-tree` without a parent. Update release `main` using
`--force-with-lease` against its previous exact commit. Verify that the private
working repository contains the history before replacing the release snapshot.
Never push development `main` directly to the release remote.

The local `refs/releases/instanto-main` ref holds the release snapshot; the release
remote's default push refspec points to that ref. Keep only `main` in the release
repository until release branches are needed. Building, testing and staging run
on local infrastructure; GitHub hosts the source and release artifacts.
