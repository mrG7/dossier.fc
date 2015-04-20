from __future__ import absolute_import, division, print_function

from dossier.fc import FeatureCollection
from dossier.fc.feature_offsets import XpathFeatureOffsets
from streamcorpus import XpathRange


# def test_fo_fcdefault():
    # fc = FeatureCollection()
    # assert isinstance(fc['@NAME'], XpathFeatureOffsets)
#
#
# def test_fo_default():
    # fo = XpathFeatureOffsets()
    # assert fo['foo'] == []
#
#
# def test_fo_roundtrip():
    # fc = FeatureCollection()
    # fc['@NAME']['foo'].append([
        # XpathRange('/p[1]/text()[1]', 0, '/p[1]/text()[1]', 3)
    # ])
    # fc2 = FeatureCollection.loads(fc.dumps())
    # assert fc['@NAME'] == fc2['@NAME']
#
#
# def test_fo_slices():
    # html = '<html><body><p><b>T</b>om <b>B</b>rady</p></body></html>'
    # root = XpathRange.html_node(html)
#
    # fo = XpathFeatureOffsets()
    # fo['tom brady'].append([
        # XpathRange('/html[1]/body[1]/p[1]/b[1]/text()[1]', 0,
                   # '/html[1]/body[1]/p[1]/text()[1]', 2),
        # XpathRange('/html[1]/body[1]/p[1]/b[2]/text()[1]', 0,
                   # '/html[1]/body[1]/p[1]/text()[2]', 4),
    # ])
    # assert fo.slices(root, 'tom brady')[0] == 'Tom Brady'
