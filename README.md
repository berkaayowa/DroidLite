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
