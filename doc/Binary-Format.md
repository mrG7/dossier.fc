dossier.fc binary format
========================

dossier.fc feature collections can be serialized to a binary format using calls like the Python ``FeatureCollection.dumps()`` method.  The actual format uses on the [Concise Binary Object Representation (CBOR)](http://cbor.io) encoding, defined in [RFC 7049](http://tools.ietf.org/html/rfc7049).  There are [CBOR implementations](http://cbor.io/impls.html) in a variety of popular programming languages.

CBOR's data model is extremely similar to JSON, and this document will generally uses JSON syntax to describe the CBOR data (see also RFC 7049 section 6).  CBOR differentiates byte strings from text strings, requiring text strings to be UTF-8-encoded and to not split multi-byte characters across chunks; dossier.fc generally uses text strings everywhere.  CBOR also adds the notion of _tags_, which label a specific item with a code indicating its intended use; dossier.fc uses these to indicate the data type of a feature.

Feature collections
-------------------

A feature collection is encoded as a list of two objects.  The first object is a map holding metadata about the feature collection object; the second object is a map holding the actual feature collection data.

    [{"v": "fc01", "ro": 1},
     {"feature": ..., "feature2": ...}]

The metadata dictionary may contain the following text-string keys:

* ``v`` indicates the version of the feature collection data.  This version always has the text-string value ``fc01``.

* ``ro`` indicates that the feature collection is intended to be read-only.  If present, it has the integer value 1.

The feature collection data is a map from text-string feature names to feature representations.  The possible representations are described in following sections.

String features
---------------

A feature may be a simple text-string.  It is represented in application code as a language-native string if possible, and in the binary serialization as a text-string.

    [{"v": "fc01"},
     {"string_feature": "any string"}]

String counter features
-----------------------

The most basic type of dossier.fc is a string counter, where a feature value is itself a mapping from a term to a count.  In the Python implementation of dossier.fc, this is provided by the ``dossier.fc.StringCounter`` class.  The binary representation is a map from text-string keys to integer values.

A string counter may be prefixed with CBOR tag 55800.  It may also appear as a bare dictionary with no tag.  On deserialization, a feature value that is an untagged dictionary will also be interpreted as a string counter.

    [{"v": "fc01"},
     {"name": {"fc": 1, "feature collection": 1},
      "abbreviation": 55800({"fc": 1})}]

Sparse vector features
---------------------

Some algorithms benefit from transforming feature collections to simple vectors of counts.  A sparse vector is one in which only a few of the vector indices are present, and the remainder are assumed to be zero.

A sparse vector is prefixed with CBOR tag 55801.  Its representation is an array of integers with even length, which are paired as zero-based indices and values.

By way of example, consider the vector ``[0, 0, 1, 0, 0, 0, 2, 0]``.  Index 2 has value 1, index 6 has value 2, and the remaining indices have value 0.  This would be represented as:

    [{"v": "fc01"},
     {"feature": 55801([2, 1, 6, 2])}]
