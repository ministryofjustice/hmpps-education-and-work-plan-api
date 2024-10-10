--- Corrects the updatedBy/At fields on `note` records previously migrated via V2024.11.09.0004__add_note_table.sql
---
--- The initial migration set the updatedBy/At fields on `note` as being the updatedBy/At fields of the parent `goal`
--- record, but there is no guarantee that the note text was updated as part of the last `goal` update.
--- By the same token there is no guarantee that the goal note text was added to the goal when the goal was first
--- created, but as part of splitting the `note` out of `goal` and treating it as an entity in its own right, complete
--- with it own created/update audit fields, we need to initialise these values as best we can.
---
--- The most common use case for PLP goals at time of writing is that a goal is created, with or without notes. It is
--- rare that goals are subsequently updated (though it does happen), but it is not believed that the goal note is
--- ever updated.
--- Therefore the best/most appropriate strategy for the initial migration of goal notes into their own `note` entity
--- is to use the goal createdBy/At fields for the new note record updatedBy/At values.
---
--- It is acknowledged that some `note` records will have incorrect created/updated audit fields, but it will be low
--- volumes, and we have no better strategy.

UPDATE note n
    SET (updated_at, updated_by, updated_at_prison) = (
        SELECT g.created_at, g.created_by, g.created_at_prison
        FROM goal g
        WHERE g.reference = n.entity_reference
    );
