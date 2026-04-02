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

    space:create_index("primary", {
        parts = { {field = "key", type = "string"} },
        if_not_exists = true
    })
end)

print("Tarantool KV initialized")