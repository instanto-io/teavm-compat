#!/usr/bin/env python3
"""Reject missing branch evidence or overlapping coverage ownership before Sonar import."""
import json
from pathlib import Path
import xml.etree.ElementTree as ET

repo = Path(__file__).resolve().parents[1]
module = repo / 'gwt-user-compat'
target = repo / 'gwt-user-compat-tests/target'
report = ET.parse(target / 'teavm-coverage.xml').getroot()
assert report.tag == 'report', 'Expected standard JaCoCo XML'
counters = {c.get('type'): {k: int(c.get(k)) for k in ['covered', 'missed']}
            for c in report.findall('counter')}
baseline = ET.parse(module / 'target/site/jacoco/jacoco.xml').getroot()
original = {c.get('type'): int(c.get('covered')) + int(c.get('missed'))
            for c in baseline.findall('counter')}
for metric in ['LINE', 'BRANCH']:
    assert counters[metric]['covered'] > 0, f'No browser/JVM {metric} evidence'
    assert sum(counters[metric].values()) == original[metric], f'Changed original {metric} denominator'
assert (target / 'teavm-coverage.xml.exec').is_file(), 'Missing raw JVM/browser union'
evidence = json.loads((target / 'compiler-evidence.json').read_text())
assert evidence['scope'] == 'instrumented-build'
assert evidence['methods'], 'Missing method reachability diagnostics'
for entry in ET.parse(repo / 'target/browser-coverage.xml').getroot().findall('file'):
    path = Path(entry.get('path'))
    if not path.is_absolute():
        path = repo / path
    assert not path.resolve().is_relative_to(module), f'Overlapping generic coverage: {path}'
print(json.dumps({'module': module.name, 'counters': counters,
                 'genericReportOverlap': False}, indent=2))
