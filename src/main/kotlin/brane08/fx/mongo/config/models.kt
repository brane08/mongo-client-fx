package brane08.fx.mongo.config

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Connections : IntIdTable() {
    val servers: Column<String> = varchar("servers", 255)
    val name: Column<String> = varchar("name", 50).uniqueIndex("connections_name_idx")
    val username: Column<String> = varchar("username", 50)
    val password: Column<String> = varchar("password", 50)
    val database: Column<String> = varchar("database", 50)
}

class Connection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Connection>(Connections)

    var servers by Connections.servers
    var name by Connections.name
    var username by Connections.username
    var password by Connections.password
    var database by Connections.database
}

object Favorites: IntIdTable() {
    val queryText: Column<String> = varchar("query_text", 1000)
}

class Favorite(id: EntityID<Int>): IntEntity(id) {
    var queryText by Favorites.queryText
}
