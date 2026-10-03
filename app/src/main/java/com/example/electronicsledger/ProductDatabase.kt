package com.example.electronicsledger

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate

class ProductDatabase(context: Context) : SQLiteOpenHelper(context, "electronics.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            price_cents INTEGER NOT NULL CHECK(price_cents >= 0),
            category TEXT NOT NULL,
            purchased_on TEXT NOT NULL
        )""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Missing database migration from $oldVersion to $newVersion")
    }

    fun all(): List<Product> = readableDatabase.query(
        "products", null, null, null, null, null, "purchased_on DESC, id DESC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(Product(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                priceCents = cursor.getLong(cursor.getColumnIndexOrThrow("price_cents")),
                category = Category.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("category"))),
                purchasedOn = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("purchased_on")))
            ))
        }
    }

    fun save(product: Product) {
        val values = ContentValues().apply {
            put("name", product.name)
            put("price_cents", product.priceCents)
            put("category", product.category.name)
            put("purchased_on", product.purchasedOn.toString())
        }
        if (product.id == 0L) writableDatabase.insertOrThrow("products", null, values)
        else check(writableDatabase.update("products", values, "id = ?", arrayOf(product.id.toString())) == 1)
    }

    fun delete(id: Long) {
        check(writableDatabase.delete("products", "id = ?", arrayOf(id.toString())) == 1)
    }

    fun changeAndRead(action: () -> Unit): List<Product> {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            action()
            val products = all()
            db.setTransactionSuccessful()
            products
        } finally {
            db.endTransaction()
        }
    }
}
