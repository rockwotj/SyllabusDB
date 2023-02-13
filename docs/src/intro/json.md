{{#title SyllabusDB | JSON}}

# JSON

Our database is going to be built upon [JSON][json], an incredibly common data-interchange format. The advantages of
basing our database on this format is that most programming languages support JSON, there are only 6 data types that we
need to support (it's simple), and is essentially ubiquitous.

JSON supports the following value types:

- `null` - sentinel value
- `boolean` - `true` or `false`
- `number` - while unspecified in the official JSON spec, we'll be treating these
  as [IEEE 754 double precision floating point numbers][ieee754]
- `string` - sequence of unicode codepoints
- `array` - an ordered list of other JSON values
- `object` - key/value pairs of strings to another JSON value

# Core Data Model

Going forward we'll use the following terminology to describe our database's core data model.

Note that many of these terms and models are similar to [MongoDB][mongo_glossary]. MongoDB supports a richer data model
and encodes certain values on top of JSON.

## Documents

Our database will specifically hold documents, which are essentially JSON objects with a special format to denote its
ID. In order to simplify our implementation, we're only going to support keys (at the top document level or in nested
JSON values) that match the regex: `[A-Za-z][A-Za-z0-9_]+`. The main avantage for doing this allows us to encode paths
to values within documents with dot delimiters (this will become more clear later).

For simplicity, we'll enforce that document IDs must match the regex `[A-Za-z][A-Za-z0-9_]+`. There are a few benefits
from doing this - we maybe able to do some special encoding tricks for examples

## Collections

Documents are grouped into collections - this allows end users of the database to create their own implicit schemas. If
you're coming from a relational database background, then you can equate collections to tables.

For simplicity, we'll enforce that collections have the same naming restrictions as document IDs and must match the
regex `[A-Za-z][A-Za-z0-9_]+`.

A full path to a document is the tuple of `(collection_id, document_id)`, as different collections are allowed to have
documents with the same ID. We will encode these with a `/` delimiter as `<collection_id>/<document_id>`.

## Example

A document in JSON form over our protocol would look like this:

```json
{
  "_id": "users/bob",
  "name": "Bob Smith",
  "username": "bobby42",
  "profile_photo": "https://example.com/avatars/bobby42",
  "age": 29,
  "contact": {
    "verified": true,
    "phone": "555-555-5555",
    "email": "bobby@example.com"
  }
}
```

## Reserved IDs

`_id` is a what is referred to as a reserved ID. Normal identifiers don't support underscores, and are reserved for
special usages within the protocol. Other examples of usages for reserved IDs would be allow querying database metadata
using reserved collection IDs (i.e. `_db_size` would be a collection with documents containing statistics on the size of
the database)

## Field Path

Path to a field in the document. To specify a field path, we'll use a dot notation that joins sequences of fields via
a `.` delimiter.

In the above example, `contact.email` is a field path that results in the string value: `"bobby@example.com"`.


[json]: https://json.org

[ieee754]: https://wikipedia.org/wiki/IEEE_754

[mongo_glossary]: https://www.mongodb.com/docs/manual/reference/glossary/