`dossier.fc` is a package that provides an implementation of feature 
collections. Abstractly, a feature collection is a map from feature name to 
a feature. While any type of feature can be supported, the core focus of this 
package is on *multisets* or "bags of words" (BOW). In this package, the 
default multiset implementation is a `StringCounter`, which maps Unicode 
strings to counts.

This package includes both [Python](python/README.md) and [Java](java/README.md) implementations of the package.

For other languages, we also document the
[binary format](doc/Binary-Format.md) of the CBOR feature collections.
