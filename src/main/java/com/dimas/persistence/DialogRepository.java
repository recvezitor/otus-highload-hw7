package com.dimas.persistence;

import com.dimas.domain.entity.DialogMessage;
import com.dimas.exception.NotFoundJdbcException;
import com.dimas.util.HashUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.dimas.util.Const.SCHEMA_NAME;
import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class DialogRepository {

    private final PgPool pgPool;

    public Uni<DialogMessage> create(DialogMessage request) {
        log.debug("persisting dialog message={}", request);
        return pgPool.withTransaction(conn -> conn.preparedQuery("""
                        INSERT INTO %s.dialog_message (dialog_id, from_user, to_user, text, created_at)
                                    VALUES ($1, $2, $3, $4, $5)
                                    RETURNING id
                        """.formatted(SCHEMA_NAME))
                .execute(map(request))
                .flatMap(rowSet -> {
                    var entity = request.withId(rowSet.iterator().next().getUUID("id"));
                    return Uni.createFrom().item(entity);
                })
        );
    }

    public Uni<DialogMessage> getById(UUID id) {
        return findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundJdbcException("DialogMessage not found for id='%s'".formatted(id)));
    }

    public Uni<DialogMessage> findById(UUID id) {
        final var query = """
                 select * from %s.dialog_message where id = $1 LIMIT 1
                """.formatted(SCHEMA_NAME);
        return pgPool.preparedQuery(query)
                .execute(Tuple.tuple().addUUID(id))
                .onItem().transformToUni(rowSet -> {
                    for (Row row : rowSet) {
                        return Uni.createFrom().item(map(row));//return first item
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    public Uni<List<DialogMessage>> dialog(UUID fromId, UUID toId) {
        log.debug("repository dialog_message");
        final var query = """
                SELECT * FROM %s.dialog_message as dm
                WHERE dm.dialog_id = $1
                ORDER BY dm.created_at DESC
                """.formatted(SCHEMA_NAME);
        return pgPool.preparedQuery(query)
                .execute(Tuple.tuple()
                        .addString(HashUtil.getDialogHash(fromId, toId))
                )
                .onItem().transformToUni(rowSet -> {
                    List<DialogMessage> result = new ArrayList<>();
                    for (Row row : rowSet) {
                        result.add(map(row));
                    }
                    return Uni.createFrom().item(result);
                });
    }

    private DialogMessage map(Row row) {
        return DialogMessage.builder()
                .id(row.getUUID("id"))
                .fromUser(row.getUUID("from_user"))
                .toUser(row.getUUID("to_user"))
                .text(row.getString("text"))
                .createdAt(row.getLocalDateTime("created_at"))
                .updatedAt(row.getLocalDateTime("updated_at"))
                .build();
    }

    private Tuple map(DialogMessage request) {
        return Tuple.tuple()
                .addUUID(request.getFromUser())
                .addUUID(request.getToUser())
                .addString(request.getText())
                .addLocalDateTime(isNull(request.getCreatedAt()) ? LocalDateTime.now() : request.getCreatedAt());
    }

}
