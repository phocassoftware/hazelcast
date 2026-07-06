# Phocas 5.7 Rebase Resolution Notes

This PR is a narrow review artifact for the manual work after replaying the
existing Phocas immutable/object-format fork onto Hazelcast 5.7.0.

The base branch already contains:

- upstream Hazelcast `v5.7.0`
- the replayed Phocas immutable patch
- the `5.7.0-phocas` Maven version suffix
- the GitHub Packages source-artifact deploy profile

The code changes in this PR are the follow-up compile/runtime/test fixes needed
after that replay.

## Conflict Areas In The Replayed Immutable Patch

The original Phocas patch was reapplied as commit `a764c62b02`. The patch shape
is mostly unchanged from the old fork commit `38fc1e1b69`, but upstream 5.7 had
moved a few nearby APIs. These were the main manual resolution points.

### Client Map Query Tasks

Several client protocol tasks had upstream changes around operation statistics
and generic result types. The Phocas patch still needed OBJECT-format values to
flow as objects internally, while protocol responses still needed `Data` where
the codec requires it.

```text
<<<<<<< old Phocas fork
List<Map.Entry<Data, Data>> entries = new ArrayList<>(result);
incrementOtherOperationsCount(getService(MapService.SERVICE_NAME), parameters);
=======
List<Map.Entry<Data, Data>> entries = new ArrayList<>(result);
MapService mapService = (MapService) getService(MapService.SERVICE_NAME);
incrementOtherOperationsCount(mapService, parameters);
>>>>>>> upstream 5.7.0

resolved:
List<Map.Entry<Data, Object>> entries = new ArrayList<>(result);
MapService mapService = (MapService) getService(MapService.SERVICE_NAME);
incrementOtherOperationsCount(mapService, parameters);
```

### EntryOperation Old-Value Conversion

Upstream 5.7 changed the surrounding return handling in
`EntryOperation.convertOldValueToHeapData`. The Phocas behavior keeps immutable
OBJECT-format values as objects, but serializes non-immutable values for copy
semantics.

```text
<<<<<<< old Phocas fork
case OBJECT:
    if (Immutable.isImmutable(oldValue)) {
        return oldValue;
    }
    return serializationService.toData(oldValue);
case BINARY:
    return oldValue;
=======
case OBJECT:
    return getNodeEngine().getSerializationService().toData(oldValue);
case BINARY:
    return oldValue;
>>>>>>> upstream 5.7.0

resolved:
case OBJECT:
    if (Immutable.isImmutable(oldValue)) {
        return oldValue;
    } else {
        return getNodeEngine().getSerializationService().toData(oldValue);
    }
case BINARY:
    return oldValue;
```

### Query Result Construction

Upstream 5.7 changed `Query.createResult` to take a
`QueryResultSizeLimiter` and partition count. The Phocas change that avoids
cloning immutable aggregators was carried forward into the new signature.

```text
<<<<<<< old Phocas fork
public Result createResult(SerializationService serializationService, long limit) {
    Aggregator aggregatorClone = aggregator;
=======
public Result createResult(SerializationService serializationService,
        QueryResultSizeLimiter qrsl, int partitions) {
    Aggregator aggregatorClone = serializationService.toObject(serializationService.toData(aggregator));
>>>>>>> upstream 5.7.0

resolved:
public Result createResult(SerializationService serializationService,
        QueryResultSizeLimiter qrsl, int partitions) {
    var aggregatorClone = aggregator;
    if (!Immutable.isImmutable(aggregatorClone)) {
        aggregatorClone = serializationService.toObject(serializationService.toData(aggregator));
    }
```

### Record Cache Classes

Upstream 5.7 switched cached record internals from
`AtomicReferenceFieldUpdater` to `VarHandle` via `ReflectionUtil`. The Phocas
patch still needed cached record values to be `Object` instead of always `Data`.

```text
<<<<<<< old Phocas fork
class CachedSimpleRecord extends SimpleRecord<Object> {
    private static final AtomicReferenceFieldUpdater<CachedSimpleRecord, Object> CACHED_VALUE = ...
=======
class CachedSimpleRecord extends SimpleRecord<Data> {
    private static final VarHandle CACHED_VALUE = ReflectionUtil.findVarHandle(...);
>>>>>>> upstream 5.7.0

resolved:
class CachedSimpleRecord extends SimpleRecord<Object> {
    private static final VarHandle CACHED_VALUE = ReflectionUtil.findVarHandle("cachedValue", Object.class);
```

The same style of resolution applies to `CachedDataRecordWithStats` and
`DataRecordWithStats`: keep the 5.7 VarHandle/ReflectionUtil structure, but keep
the Phocas `Object` value type.

### Partition-Wide Entry Operations

Upstream 5.7 changed partition-wide entry processing to use
`operateOnKeyValueDuringScan`. The Phocas patch changed responses from `Data` to
`Object`. Both changes were retained.

```text
<<<<<<< old Phocas fork
Object response = operator.operateOnKey(dataKey).getResult();
=======
Data response = operator.operateOnKeyValueDuringScan(dataKey, record.getValue()).getResult();
>>>>>>> upstream 5.7.0

resolved:
Object response = operator.operateOnKeyValueDuringScan(dataKey, record.getValue()).getResult();
```

## Follow-Up Fixes In This PR

After the replayed patch compiled, the focused test runs exposed these additional
changes:

- `MapGetEntryViewMessageTask` now converts OBJECT-format entry-view key/value
  objects to `Data` before calling the client protocol codec.
- Tests that assert no OBJECT-format deserialization now mark their sentinel
  values as `Immutable`, matching the Phocas copy-semantics contract.
- `IndexDeserializationTest` now expects zero deserializations for OBJECT-format
  index updates under the Phocas behavior.
- `CheckDependenciesIT` excludes Surefire boot manifests when looking for the
  Hazelcast manifest.
