`dossier.fc` is a Python package that provides an implementation of feature 
collections. Abstractly, a feature collection is a map from feature name to 
a feature. While any type of feature can be supported, the core focus of this 
package is on *multisets* or "bags of words" (BOW). In this package, the 
default multiset implementation is a `StringCounter`, which maps Unicode 
strings to counts.

This package also includes utilities for serializing and deserializating 
collections of feature collections to CBOR (Concise Binary Object 
Representation).


### Installation

`dossier.fc` is on PyPI and can be installed with `pip`:

```bash
pip install dossier.fc
```

Currently, `dossier.fc` requires Python 2.7. It is not yet Python 3 compatible.


### Documentation

API documentation with examples is available as part of the Dossier Stack 
documentation: http://diffeo.com/dossier-stack


### Examples and basic usage

By default, feature collections use `StringCounter` for feature representation:

```python
from dossier.fc import FeatureCollection

fc = FeatureCollection()
fc['NAME']['alice'] += 1
fc['NAME']['bob'] = 5

print type(fc['NAME'])
# output: StringCounter
```

As the name suggests, a `StringCounter` is a subclass of the `Counter` class 
found in the Python standard library `collections` module. As such, it supports 
binary operations like addition, subtraction, equality testing, etc. 
The `FeatureCollection` class also provides these binary operations which are 
performed on corresponding features. For example:

```python
from dossier.fc import FeatureCollection

fc1 = FeatureCollection({'NAME': {'alice': 1, 'bob': 1}})
fc2 = FeatureCollection({'NAME': {'alice': 1}})
fc3 = fc1 + fc2

assert fc3 == FeatureCollection({'NAME': {'alice': 2, 'bob': 1}})
```

### Java Implementation

A Java implementation of `dossier.fc` is in the ``java`` directory.  It builds with [Apache Maven](http://maven.apache.org/).  If you are unfamiliar with Maven, install it and run ``mvn package`` from the ``java`` directory; this will produce a jar file ``java/target/fc-0.1-SNAPSHOT.jar``.

This implementation depends on the [Jackson](https://github.com/FasterXML/jackson) library for serialization and deserialization, and in particular, the [jackson-dataformat-cbor](https://github.com/FasterXML/jackson-dataformat-cbor) extension for CBOR support.  This makes serializing and deserializing feature collections straightforward:

```java
import com.diffeo.dossier.fc.FeatureCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import java.io.File;

FeatureCollection fc = new FeatureCollection();

CBORFactory cborf = new CBORFactory();
ObjectMapper mapper = new ObjectMapper(cborf);
File f = new File("output.fc");
mapper.writeValue(f, fc);
```

Chunk files as written by the Python `FeatureCollectionCborChunk` class are simply concatenated CBOR objects.  These can also be easily read and written using the Jackson `ObjectMapper`:

```java
import java.io.FileInputStream;
import java.io.FileOutputStream;

FileOutputStream fos = new FileOutputStream("output.fc");
mapper.writeValue(fos, fc1);
mapper.writeValue(fos, fc2);
fos.close();

FileInputStream fis = new FileInputStream("output.fc");
FeatureCollection fc = mapper.readValue(fis, FeatureCollection.class);
assert fc.equals(fc1);
fc = mapper.readValue(fis, FeatureCollection.class);
assert fc.equals(fc2);
fis.close();

fis = new FileInputStream("output.fc");
for (fc: mapper.readValues(fis, FeatureCollection.class)) {
}
fis.close();
```

This implementation depends on CBOR tag support, which is not in a released version of jackson-dataformat-cbor as of this writing.  The Maven `pom.xml` file depends on a pre-release version of this module, and correspondingly, this package is not in the Maven central repository.

*Note:* If you create a Jackson `ObjectMapper` object without passing in a `CBORFactory` parameter, Jackson will read and write JSON instead.  The JSON representation of feature collections should be largely semantically equivalent to the CBOR representation; however, it will not be compatible with other CBOR-based implementations.  This should only be used for debugging purposes.
