import re

base = set(re.findall(r'<string name="([^"]+)"', open('app/src/main/res/values/strings.xml', encoding='utf-8').read()))
ar = set(re.findall(r'<string name="([^"]+)"', open('app/src/main/res/values-ar/strings.xml', encoding='utf-8').read()))
fr = set(re.findall(r'<string name="([^"]+)"', open('app/src/main/res/values-fr/strings.xml', encoding='utf-8').read()))

print('AR missing:', sorted(base - ar))
print('FR missing:', sorted(base - fr))
