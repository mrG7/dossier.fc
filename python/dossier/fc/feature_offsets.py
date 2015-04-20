from __future__ import absolute_import, division, print_function

import abc
from collections import MutableMapping

from streamcorpus import XpathRange


class FeatureOffsets(MutableMapping):
    '''Represents offsets for features into source material.

    This abstraction represents a map from feature name to a sequence
    of offset sequences. That is, each feature name maps to zero or
    more sequences of offsets, where each offset sequence corresponds
    to a possibly non-contiguous region of text in the source material.
    This region of text should correspond to the place where the
    feature was extracted.

    This is an abstract base class. In particular, the representation
    of the offset and the type of the source material are left as
    implementation details.
    '''
    __metaclass__ = abc.ABCMeta

    def __init__(self):
        self._ranges = {}

    @abc.abstractmethod
    def slices(self, source, k):
        '''Return source text for some feature value ``k``.

        ``source``'s type is unspecified and depends on the type
        of data being addressed by offsets.
        '''
        raise NotImplementedError()

    def __getitem__(self, k):
        return self._ranges.get(uni(k)) or self.__missing__(k)

    def __missing__(self, k):
        v = []
        self[uni(k)] = v
        return v

    def __setitem__(self, k, v): self._ranges[uni(k)] = v
    def __delitem__(self, k): del self._ranges[uni(k)]
    def __len__(self): return len(self._ranges)
    def __iter__(self): return iter(self._ranges)

    def __repr__(self):
        return '%s(%s)' % (self.__class__.__name__, repr(self._ranges))


class XpathFeatureOffsets(FeatureOffsets):
    '''Provides Xpath offsets into HTML.'''
    def slices(self, source, k):
        slices = []
        for xpranges in self[k]:
            slices.append(' '.join(xp.slice_node(source) for xp in xpranges))
        return slices

    def to_dict(self):
        return XpathFeatureOffsetsSerializer.dumps(self)


class XpathFeatureOffsetsSerializer(object):
    '''Naive serialization for Xpath offsets.

    This represents a range of xpaths as a four-tuple: ``(start xpath,
    start char offset, end xpath, end char offset)``.

    This will serialize to and from :class:`streamcorpus.XpathRange`
    objects.
    '''
    def __init__(self):
        raise NotImplementedError()

    constructor = XpathFeatureOffsets

    @staticmethod
    def loads(d):
        fo = XpathFeatureOffsets()
        for fname in d:
            for xpranges in d[fname]:
                xps = []
                for xpr in xpranges:
                    if xpr is None:
                        xps.append(None)
                    else:
                        xp1, i1, xp2, i2 = xpr
                        xps.append(XpathRange(xp1, i1, xp2, i2))
                fo[fname].append(xps)
        return fo

    @staticmethod
    def dumps(o):
        d = {}
        for fname in o:
            d[fname] = []
            for xpranges in o[fname]:
                tuples = []
                for xp in xpranges:
                    if xp is None:
                        tuples.append(None)
                    else:
                        tuples.append((uni(xp.start_xpath), xp.start_offset,
                                       uni(xp.end_xpath), xp.end_offset))
                d[fname].append(tuples)
        return d


def uni(s):
    if isinstance(s, str):
        return unicode(s, 'utf-8')
    elif isinstance(s, unicode):
        return s
    else:
        raise TypeError(s)
