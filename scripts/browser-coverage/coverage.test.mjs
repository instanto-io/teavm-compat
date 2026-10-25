import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mergeCoverage, coverageXml } from './coverage.mjs';

const map = { version: 3, sources: ['demo/Api.java'], names: [], mappings: 'AAAA;AACA;AACA' };
const paths = new Map([['demo/Api.java', 'runtime/src/main/java/demo/Api.java']]);
const entry = {
  source: 'a\nb\nc\n',
  functions: [{ ranges: [
    { startOffset: 0, endOffset: 6, count: 1 },
    { startOffset: 2, endOffset: 4, count: 0 }
  ] }]
};
test('unexecuted inner blocks remain uncovered despite an executed enclosing function', () => {
  const hits = new Map();
  mergeCoverage(hits, entry, map, paths);
  assert.deepEqual([...hits.get(paths.get('demo/Api.java'))], [[1, true], [2, false], [3, true]]);
});
test('separate test executions accumulate hits without erasing previously covered lines', () => {
  const hits = new Map();
  mergeCoverage(hits, entry, map, paths);
  mergeCoverage(hits, { ...entry, functions: [{ ranges: [{ startOffset: 2, endOffset: 4, count: 1 }] }] }, map, paths);
  assert.deepEqual([...hits.values()][0], new Map([[1, true], [2, true], [3, true]]));
});
test('third-party and unmapped locations cannot inflate owned source coverage', () => {
  const hits = new Map();
  mergeCoverage(hits, entry, { ...map, sources: ['external/Api.java'] }, paths);
  assert.equal(hits.size, 0);
});
test('Sonar output preserves missed lines and escapes source paths', () => {
  const xml = coverageXml(new Map([['a&b"c.java', new Map([[3, false], [1, true]])]]));
  assert.match(xml, /path="a&amp;b&quot;c.java"/);
  assert.match(xml, /lineNumber="3" covered="false"/);
  assert.ok(xml.indexOf('lineNumber="1"') < xml.indexOf('lineNumber="3"'));
});
