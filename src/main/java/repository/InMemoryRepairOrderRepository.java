package repository;

import domain.RepairOrder;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class InMemoryRepairOrderRepository implements RepairOrderRepository {
    private final Map<String, RepairOrder> storage = new LinkedHashMap<>();

    @Override
    public <S extends RepairOrder> S save(S order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public <S extends RepairOrder> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(entity -> result.add(save(entity)));
        return result;
    }

    @Override
    public Optional<RepairOrder> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }

    @Override
    public List<RepairOrder> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<RepairOrder> findAllById(Iterable<String> ids) {
        List<String> idList = new ArrayList<>();
        ids.forEach(idList::add);
        return idList.stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }

    @Override
    public void delete(RepairOrder entity) {
        if (entity != null) {
            storage.remove(entity.getId());
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        ids.forEach(storage::remove);
    }

    @Override
    public void deleteAll(Iterable<? extends RepairOrder> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    @Override
    public List<RepairOrder> findAllByClientId(String clientId) {
        return storage.values().stream()
                .filter(order -> order.getClient() != null && order.getClient().getId().equals(clientId))
                .toList();
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends RepairOrder> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends RepairOrder> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<RepairOrder> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<String> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public RepairOrder getOne(String id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("Not found: " + id));
    }

    @Override
    public RepairOrder getById(String id) {
        return getOne(id);
    }

    @Override
    public RepairOrder getReferenceById(String id) {
        return getOne(id);
    }

    @Override
    public List<RepairOrder> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<RepairOrder> findAll(Pageable pageable) {
        List<RepairOrder> all = findAll();
        return new PageImpl<>(all, pageable, all.size());
    }

    @Override
    public <S extends RepairOrder> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }

    @Override
    public <S extends RepairOrder> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }

    @Override
    public <S extends RepairOrder> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }

    @Override
    public <S extends RepairOrder> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }

    @Override
    public <S extends RepairOrder> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }

    @Override
    public <S extends RepairOrder> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }

    @Override
    public <S extends RepairOrder, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Example queries are not supported in in-memory repository.");
    }
}
