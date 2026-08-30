import Database from 'better-sqlite3';
import fs from 'node:fs';
import path from 'node:path';

const file = process.env.DB_FILE || './data/lagan.db';
fs.mkdirSync(path.dirname(file), { recursive: true });
export const db = new Database(file);
db.pragma('foreign_keys = ON');

db.exec(`
CREATE TABLE IF NOT EXISTS restaurants (id INTEGER PRIMARY KEY, name TEXT NOT NULL, slug TEXT UNIQUE NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS admins (id INTEGER PRIMARY KEY, restaurant_id INTEGER NOT NULL REFERENCES restaurants(id), name TEXT NOT NULL, pin_hash TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'admin');
CREATE TABLE IF NOT EXISTS tables (id INTEGER PRIMARY KEY, restaurant_id INTEGER NOT NULL REFERENCES restaurants(id), number INTEGER NOT NULL, sort_order INTEGER NOT NULL, active INTEGER NOT NULL DEFAULT 1, UNIQUE(restaurant_id, number));
CREATE TABLE IF NOT EXISTS menu_items (id INTEGER PRIMARY KEY, restaurant_id INTEGER NOT NULL REFERENCES restaurants(id), name TEXT NOT NULL, price INTEGER NOT NULL, category TEXT NOT NULL DEFAULT 'Asosiy', active INTEGER NOT NULL DEFAULT 1, sort_order INTEGER NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY, restaurant_id INTEGER NOT NULL REFERENCES restaurants(id), table_id INTEGER NOT NULL REFERENCES tables(id), status TEXT NOT NULL DEFAULT 'new' CHECK(status IN ('new','seen','accepted','completed','cancelled')), customer_note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP, seen_at TEXT);
CREATE TABLE IF NOT EXISTS order_items (id INTEGER PRIMARY KEY, order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE, menu_item_id INTEGER REFERENCES menu_items(id), name TEXT NOT NULL, price INTEGER NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0));
`);

const restaurant = db.prepare('SELECT id FROM restaurants WHERE slug = ?').get('lagan');
if (!restaurant) {
  const r = db.prepare('INSERT INTO restaurants (name, slug) VALUES (?, ?)').run('Lagan', 'lagan');
  const id = r.lastInsertRowid;
  db.prepare('INSERT INTO admins (restaurant_id,name,pin_hash) VALUES (?,?,?)').run(id, 'Asosiy admin', process.env.DEFAULT_ADMIN_PIN || '1234');
  const table = db.prepare('INSERT INTO tables (restaurant_id,number,sort_order) VALUES (?,?,?)');
  for (let n = 1; n <= 12; n++) table.run(id, n, n);
  const item = db.prepare('INSERT INTO menu_items (restaurant_id,name,price,category,sort_order) VALUES (?,?,?,?,?)');
  [['Osh',35000,'Taomlar'],['Lag‘mon',32000,'Taomlar'],['Somsa',8000,'Yeguliklar'],['Salat',15000,'Salatlar'],['Choy',5000,'Ichimliklar'],['Cola',10000,'Ichimliklar']].forEach((x,i) => item.run(id,...x,i));
}
