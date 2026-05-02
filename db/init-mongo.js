const databaseName = process.env.MONGO_DATABASE || "retail_discount_db"
const appUsername = process.env.MONGO_APP_USERNAME || "retail_app_user"
const appPassword = process.env.MONGO_APP_PASSWORD || "retail_app_password_123"

db = db.getSiblingDB(databaseName)

print("Dropping database: " + databaseName)
db.dropDatabase()

db = db.getSiblingDB(databaseName)

// ============================================================
// USERS COLLECTION
// ============================================================

db.createCollection("users", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["userId", "userType", "createdAt", "updatedAt"],
            properties: {
                userId: {
                    bsonType: "long"
                },
                userType: {
                    enum: ["EMPLOYEE", "AFFILIATE", "CUSTOMER"]
                },
                customerSince: {
                    bsonType: ["date", "null"]
                },
                createdAt: {
                    bsonType: "date"
                },
                updatedAt: {
                    bsonType: "date"
                }
            }
        }
    },
    validationAction: "error",
    validationLevel: "strict"
})

db.users.createIndex({ userId: 1 }, { unique: true })
db.users.createIndex({ userType: 1 })

// ============================================================
// ITEMS COLLECTION
// ============================================================

db.createCollection("items", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["itemId", "name", "category", "unitPrice", "createdAt", "updatedAt"],
            properties: {
                itemId: {
                    bsonType: "long"
                },
                name: {
                    bsonType: "string"
                },
                category: {
                    enum: ["GROCERY", "OTHER"]
                },
                unitPrice: {
                    bsonType: "decimal"
                },
                createdAt: {
                    bsonType: "date"
                },
                updatedAt: {
                    bsonType: "date"
                }
            }
        }
    },
    validationAction: "error",
    validationLevel: "strict"
})

db.items.createIndex({ itemId: 1 }, { unique: true })
db.items.createIndex({ category: 1 })

// ============================================================
// BILLS COLLECTION
// Uses MongoDB _id as billId in API response.
// No separate billId field.
// ============================================================

db.createCollection("bills", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["user", "items", "calculation", "createdAt"],
            properties: {
                user: {
                    bsonType: "object",
                    required: ["userId", "userType"],
                    properties: {
                        userId: {
                            bsonType: "long"
                        },
                        userType: {
                            enum: ["EMPLOYEE", "AFFILIATE", "CUSTOMER"]
                        },
                        customerSince: {
                            bsonType: ["date", "null"]
                        }
                    }
                },

                items: {
                    bsonType: "array",
                    minItems: 1,
                    items: {
                        bsonType: "object",
                        required: ["itemId", "name", "category", "unitPrice", "quantity", "totalItemPrice"],
                        properties: {
                            itemId: {
                                bsonType: "long"
                            },
                            name: {
                                bsonType: "string"
                            },
                            category: {
                                enum: ["GROCERY", "OTHER"]
                            },
                            unitPrice: {
                                bsonType: "decimal"
                            },
                            quantity: {
                                bsonType: "int",
                                minimum: 1
                            },
                            totalItemPrice: {
                                bsonType: "decimal"
                            }
                        }
                    }
                },

                calculation: {
                    bsonType: "object",
                    required: ["billAmount", "appliedDiscountType", "discountAmount", "netPayableAmount"],
                    properties: {
                        billAmount: {
                            bsonType: "decimal"
                        },
                        appliedDiscountType: {
                            enum: ["EMPLOYEE", "AFFILIATE", "CUSTOMER_OVER_TWO_YEARS", "NONE"]
                        },
                        discountAmount: {
                            bsonType: "decimal"
                        },
                        netPayableAmount: {
                            bsonType: "decimal"
                        }
                    }
                },

                createdAt: {
                    bsonType: "date"
                }
            }
        }
    },
    validationAction: "error",
    validationLevel: "strict"
})

db.bills.createIndex({ "user.userId": 1 })
db.bills.createIndex({ createdAt: -1 })
db.bills.createIndex({ "items.itemId": 1 })

// ============================================================
// SAMPLE USERS
// ============================================================

db.users.insertMany([
    {
        userId: NumberLong("1001"),
        userType: "EMPLOYEE",
        customerSince: ISODate("2020-01-15T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1002"),
        userType: "AFFILIATE",
        customerSince: ISODate("2022-03-10T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1003"),
        userType: "CUSTOMER",
        customerSince: ISODate("2021-01-01T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1004"),
        userType: "CUSTOMER",
        customerSince: ISODate("2025-01-01T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1005"),
        userType: "EMPLOYEE",
        customerSince: ISODate("2019-06-20T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1006"),
        userType: "AFFILIATE",
        customerSince: ISODate("2023-08-05T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1007"),
        userType: "CUSTOMER",
        customerSince: ISODate("2020-11-11T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1008"),
        userType: "CUSTOMER",
        customerSince: ISODate("2024-09-01T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1009"),
        userType: "CUSTOMER",
        customerSince: ISODate("2022-02-14T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        userId: NumberLong("1010"),
        userType: "AFFILIATE",
        customerSince: ISODate("2021-12-30T00:00:00Z"),
        createdAt: new Date(),
        updatedAt: new Date()
    }
])

// ============================================================
// SAMPLE ITEMS
// ============================================================

db.items.insertMany([
    {
        itemId: NumberLong("2001"),
        name: "Rice",
        category: "GROCERY",
        unitPrice: NumberDecimal("50.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2002"),
        name: "Headphones",
        category: "OTHER",
        unitPrice: NumberDecimal("300.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2003"),
        name: "T-Shirt",
        category: "OTHER",
        unitPrice: NumberDecimal("120.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2004"),
        name: "Milk",
        category: "GROCERY",
        unitPrice: NumberDecimal("15.50"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2005"),
        name: "Laptop Bag",
        category: "OTHER",
        unitPrice: NumberDecimal("180.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2006"),
        name: "Bread",
        category: "GROCERY",
        unitPrice: NumberDecimal("8.75"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2007"),
        name: "Smart Watch",
        category: "OTHER",
        unitPrice: NumberDecimal("450.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2008"),
        name: "Eggs",
        category: "GROCERY",
        unitPrice: NumberDecimal("22.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2009"),
        name: "Sneakers",
        category: "OTHER",
        unitPrice: NumberDecimal("250.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        itemId: NumberLong("2010"),
        name: "Keyboard",
        category: "OTHER",
        unitPrice: NumberDecimal("99.00"),
        createdAt: new Date(),
        updatedAt: new Date()
    }
])

// ============================================================
// APPLICATION USER
// ============================================================

const existingUsers = db.getUsers().users.map(user => user.user)

if (!existingUsers.includes(appUsername)) {
    db.createUser({
        user: appUsername,
        pwd: appPassword,
        roles: [
            {
                role: "readWrite",
                db: databaseName
            }
        ]
    })

    print("Application MongoDB user created successfully: " + appUsername)
} else {
    print("Application MongoDB user already exists: " + appUsername)
}

// ============================================================
// VERIFY
// ============================================================

print("Database initialized successfully.")
print("Database:")
printjson(db.getName())

print("Collections:")
printjson(db.getCollectionNames())

print("Users count:")
printjson(db.users.countDocuments())

print("Items count:")
printjson(db.items.countDocuments())

print("Bills count:")
printjson(db.bills.countDocuments())

print("Bill indexes:")
printjson(db.bills.getIndexes())

print("Mongo users:")
printjson(db.getUsers())