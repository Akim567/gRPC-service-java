box.cfg{
    listen = 3301
}

box.once("init_kv_space", function()
    local space = box.schema.space.create("KV", {
        if_not_exists = true
    })

    space:format({
        {name = "key", type = "string"},
        {name = "value", type = "varbinary", is_nullable = true}
    })

    -- Primary index for fast key lookup
    space:create_index("primary", {
        parts = { {field = "key", type = "string"} },
        if_not_exists = true
    })

    -- Secondary index for range queries (optimized for pairs iteration)
    space:create_index("key_range", {
        parts = { {field = "key", type = "string"} },
        type = "tree",
        if_not_exists = true
    })
end)

box.once("init_app_user", function()
    box.schema.user.create("app", {
        password = "app",
        if_not_exists = true
    })

    box.schema.user.grant("app", "read,write", "space", "KV", {
        if_not_exists = true
    })

    box.schema.user.grant("app", "session,usage", "universe", nil, {
        if_not_exists = true
    })

    box.schema.user.grant("app", "execute", "universe", nil, {
        if_not_exists = true
    })
end)

print("Tarantool KV initialized")