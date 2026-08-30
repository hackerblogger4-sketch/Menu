import ExcelJS from 'exceljs';
import { db } from './db.js';

const toDate = value => value.toISOString().slice(0, 10);
export function orderReport(restaurantId, from, to) {
  const rows = db.prepare(`SELECT o.id, o.created_at, o.status, o.customer_note, t.number table_number,
    GROUP_CONCAT(oi.quantity || ' × ' || oi.name, ', ') items, SUM(oi.quantity * oi.price) total
    FROM orders o JOIN tables t ON t.id=o.table_id JOIN order_items oi ON oi.order_id=o.id
    WHERE o.restaurant_id=? AND date(o.created_at) BETWEEN date(?) AND date(?)
    GROUP BY o.id ORDER BY o.created_at DESC`).all(restaurantId, from, to);
  return rows;
}
export async function buildOrderWorkbook(restaurantName, restaurantId, from=toDate(new Date()), to=from) {
  const workbook = new ExcelJS.Workbook();
  workbook.creator = 'Lagan QR';
  const sheet = workbook.addWorksheet('Buyurtmalar');
  sheet.columns = [
    { header: 'Buyurtma #', key: 'id', width: 14 }, { header: 'Sana va vaqt', key: 'created_at', width: 22 },
    { header: 'Stol', key: 'table_number', width: 10 }, { header: 'Taomlar', key: 'items', width: 42 },
    { header: 'Jami (so‘m)', key: 'total', width: 16 }, { header: 'Holat', key: 'status', width: 15 }, { header: 'Izoh', key: 'customer_note', width: 28 }
  ];
  sheet.mergeCells('A1:G1'); sheet.getCell('A1').value = `${restaurantName} — buyurtmalar hisoboti (${from} — ${to})`;
  sheet.getCell('A1').font = { bold: true, size: 14 }; sheet.getCell('A1').alignment = { horizontal: 'center' };
  const header = sheet.getRow(2); sheet.spliceRows(2, 0, sheet.columns.map(x => x.header));
  sheet.getRow(2).font = { bold: true }; sheet.getRow(2).fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF963B18' } }; sheet.getRow(2).font = { bold: true, color: { argb: 'FFFFFFFF' } };
  const rows = orderReport(restaurantId, from, to); rows.forEach(row => sheet.addRow(row));
  sheet.getColumn('total').numFmt = '#,##0'; sheet.autoFilter = 'A2:G2'; sheet.views = [{ state: 'frozen', ySplit: 2 }];
  return { buffer: await workbook.xlsx.writeBuffer(), count: rows.length };
}
