# Globally Unique ID Generator

[![license](http://img.shields.io/badge/license-MIT-red.svg?style=flat)](https://raw.githubusercontent.com/0xShamil/java-xid/master/LICENSE) [![Build Status](https://travis-ci.org/0xShamil/java-xid.svg?branch=master)](https://travis-ci.org/0xShamil/java-xid) [![codecov](https://codecov.io/gh/0xShamil/java-xid/branch/master/graph/badge.svg)](https://codecov.io/gh/0xShamil/java-xid)



###### This project is a Java implementation of the Go Lang library found here: [https://github.com/rs/xid](https://github.com/rs/xid)

---

## Description

`Xid` is a globally unique id generator library. They are small, fast to generate and ordered.

Xid uses the *Mongo Object ID* algorithm to generate globally unique ids with a different serialization (base32) to make it shorter when transported as a string:
https://docs.mongodb.org/manual/reference/object-id/

<table border="1">
<caption>Xid layout</caption>
<tr>
<td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td>
</tr>
<tr>
<td colspan="4">time</td><td colspan="5">random value</td><td colspan="3">inc</td>
</tr>
</table>

- a 4-byte value representing the seconds since the Unix epoch
- a 5-byte random value
- a 3-byte incrementing counter, initialized to a random value

The binary representation of the id is compatible with Mongo 12 bytes Object IDs.
The string representation is using [base32 hex (w/o padding)](https://tools.ietf.org/html/rfc4648#page-10) for better space efficiency when stored in that form (20 bytes). The hex variant of base32 is used to retain the
sortable property of the id.

`Xid`s simply offer uniqueness and speed, but they are not cryptographically secure. They are predictable and can be *brute forced* given enough time.

## Features
- Size: 12 bytes (96 bits), smaller than UUID, larger than [Twitter Snowflake](https://blog.twitter.com/2010/announcing-snowflake)
- Base32 hex encoded by default (20 chars when transported as printable string, still sortable)
- Configuration free: there is no need to set a unique machine and/or data center id
- K-ordered
- Embedded time with 1 second precision
- Unicity guaranteed for 16,777,216 (24 bits) unique ids per second and per host/process
- Lock-free (unlike UUIDv1 and v2)

## Comparison

| Name        | Binary Size | String Size    | Features
|-------------|-------------|----------------|----------------
| [UUID]      | 16 bytes    | 36 chars       | configuration free, not sortable
| [shortuuid] | 16 bytes    | 22 chars       | configuration free, not sortable
| [Snowflake] | 8 bytes     | up to 20 chars | needs machine/DC configuration, needs central server, sortable
| [MongoID]   | 12 bytes    | 24 chars       | configuration free, sortable
| xid         | 12 bytes    | 20 chars       | configuration free, sortable

[UUID]: https://en.wikipedia.org/wiki/Universally_unique_identifier
[shortuuid]: https://github.com/stochastic-technologies/shortuuid
[Snowflake]: https://blog.twitter.com/2010/announcing-snowflake
[MongoID]: https://docs.mongodb.org/manual/reference/object-id/

## Usage
Get `Xid` instance
```java
final Xid xid = Xid.get(); 

System.out.println(xid.toString()); // 9m4e2mr0ui3e8a215n4g
```
as base32Hex `String` 

```java
final String xidStr = Xid.string(); // bt0j9l2s5bo37fcla7q0
```
as `byte` array:

```java
final byte[] xidBytes = Xid.bytes(); 
```

to create an `Xid` from a specific date

```java
final String d = "10-Aug-2020 09:43:29 +0000"; 
final String dateFormat  = "dd-MMM-yyyy HH:mm:ss Z";
Date date = new SimpleDateFormat(dateFormat).parse(d);
final Xid xid = new Xid(date);

System.out.println(xid.toString()); // bsohdgdl8njn9eimov6g
System.out.println(xid.getDate()); // Mon Aug 10 15:13:29 IST 2020
System.out.println(xid.getTimestamp()); // 1597052609
```

to construct back `Xid` from a hex string:
```java
final Xid xid = new Xid("bsohdgdl8njn9eimov6g");

System.out.println(xid.getDate()); // Mon Aug 10 15:13:29 IST 2020
System.out.println(xid.getTimestamp()); // 1597052609
```

## Licenses
The source code is licensed under the [MIT License](https://github.com/0xShamil/java-xid/master/LICENSE).
