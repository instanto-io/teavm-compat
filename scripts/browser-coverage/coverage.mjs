import { TraceMap, eachMapping } from '@jridgewell/trace-mapping';

/** Map actual V8 execution counts to original Java lines, including missed blocks. */
export function mergeCoverage(hits, entry, sourceMap, sourcePaths) {
  const lineStarts = [0];
  for (let offset = 0; offset < entry.source.length; offset++) {
    if (entry.source[offset] === '\n') lineStarts.push(offset + 1);
  }
  const ranges = entry.functions.flatMap(fn => fn.ranges)
    .sort((a, b) => (a.endOffset - a.startOffset) - (b.endOffset - b.startOffset));
  eachMapping(new TraceMap(sourceMap), mapping => {
    const source = sourcePaths.get(mapping.source);
    if (!source || !mapping.originalLine) return;
    const offset = lineStarts[mapping.generatedLine - 1] + mapping.generatedColumn;
    // The innermost block overrides enclosing function/script counts, including zero counts.
    const range = ranges.find(candidate => candidate.startOffset <= offset && offset < candidate.endOffset);
    const covered = (range?.count ?? 0) > 0;
    if (!hits.has(source)) hits.set(source, new Map());
    const lines = hits.get(source);
    lines.set(mapping.originalLine, (lines.get(mapping.originalLine) ?? false) || covered);
  });
}

export function coverageXml(hits) {
  const escape = value => value.replaceAll('&', '&amp;').replaceAll('"', '&quot;').replaceAll('<', '&lt;');
  const xml = ['<coverage version="1">'];
  for (const [file, lines] of [...hits].sort()) {
    xml.push(`<file path="${escape(file)}">`);
    for (const [line, covered] of [...lines].sort((a, b) => a[0] - b[0])) {
      xml.push(`<lineToCover lineNumber="${line}" covered="${covered}"/>`);
    }
    xml.push('</file>');
  }
  xml.push('</coverage>');
  return xml.join('\n') + '\n';
}
