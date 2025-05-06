# DroidLite ORM [![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE) [![Version](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)](CHANGELOG.md)

A lightweight Android ORM (Object Relational Mapping) library for SQLite database operations with minimal boilerplate code.

---

## 📦 Table of Contents
1. [Installation](#installation)
2. [Model Definition](#model-definition)
3. [Annotations Guide](#annotations-guide)
4. [CRUD Operations](#crud-operations)
5. [Query Builder & Filters](#query-builder--filters)
6. [Key Features](#key-features)
7. [Notes](#notes)
8. [Contributing](#contributing)
9. [License](#license)
10. [Author](#author)

---

## 🚀 Installation

### 1. Initialize in Application Class
Add this to your `Application` class:
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        DroidLiteSetup.dataBase(this, "app.db", new Class[]{
            User.class  // Register your entity classes here
        }, true); // Enable logging
    }
}
```
### 2. Model Definition
```java
@Table(Name = "tUser")
public class User extends Entity {
    public User() {} // Required default constructor

    // Constructor to load by ID
    public User(int id) {
        bind(getById(id));
    }

    @Column(Name = "Id", Constraint = {Constraint.PrimaryKey})
    public int Id;

    @Column(Constraint = {Constraint.Null})
    public String Name;

    @Column
    public float Salary;

    @Column
    public double HourWorked;

    @Column(Constraint = {Constraint.Null})
    public boolean IsDeleted;

    // Custom get methods
    public IEntity getById(int id) {
        return getFirstOrNull(new TableColumn[]{new TableColumn("Id", id)});
    }

    public ArrayList<IEntity> getByName(String name) {
        return get(new TableColumn[]{new TableColumn("Name", name)});
    }

    public static ArrayList<IEntity> getAll() {
        return Entity.getAll(User.class, new TableColumn[]{});
    }
}
```
### 3. Annotation Guide
| Annotation    | Description | Example |
| -------- | ------- |-------|
| @Table  | Maps class to database table    |@Table(Name = "tUser")|
| @Column | Maps field to table column     |@Column(Constraint = {...})|
| @Constraint.*   | 	Column constraints    |Constraint.PrimaryKey|

### 4. CRUD Operations
#### Create Record
```java
User user = new User();
user.Id = 0; // 0 = new record
user.Name = "John Doe";
user.Salary = 50000;
user.HourWorked = 40.5;

if (user.save()) {
    Log.d("Success", "User created!");
}
```
#### Read Records
```java
// Get by ID
User user = new User(1);

// Get all records
ArrayList<IEntity> allUsers = User.getAll();

// Custom query
ArrayList<IEntity> results = User.get(new TableColumn[]{
    new TableColumn("Salary", ComparisonOperator.GreaterThan, 30000)
});
```
#### Update Record
```java
User user = new User(1);
user.Salary = 55000;
user.save(); // Automatically updates existing record
```
#### Delete Record
```java
User user = new User(1);
user.delete(); 
```

#### Filter Examples
```java
// Single condition
new TableColumn("Name", "Alice");

// Range condition
new TableColumn("Salary", ComparisonOperator.GreaterThan, 30000);

// Complex queries
TableColumn[] filters = new TableColumn[]{
    new TableColumn("Salary", ComparisonOperator.GreaterThan, 30000),
    new TableColumn("IsDeleted", false)
};
ArrayList<IEntity> filtered = User.get(filters);
```

### 4. Key Features
✅ Automatic Table Creation
✅ Zero Version Management
✅ Simple Annotation API
✅ Basic Type Support (int, float, double, String, boolean)
✅ Lightweight (no external dependencies)

### 5. Notes
Fields must be declared public
Requires Android API 16+
Database operations run on main thread (use background threads for production)
No migration support (ideal for small apps)
Logging enabled via last setup parameter

### 5. Contributing
Fork the project
Create your feature branch (git checkout -b feature/new-feature)
Commit your changes (git commit -am 'Add new feature')
Push to the branch (git push origin feature/new-feature)
Create new Pull Request

### 6. License
MIT License

Copyright (c) 2023 Berka Ayowa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

Author
Berka Ayowa
