import type { ReactNode } from "react";

export interface Column<T> {
  header: string;
  render: (row: T) => ReactNode;
  className?: string;
}

export function DataTable<T>({
  columns,
  rows,
  keyFn,
  onRowClick,
  emptyMessage = "Sin resultados",
}: {
  columns: Column<T>[];
  rows: T[];
  keyFn: (row: T) => string | number;
  onRowClick?: (row: T) => void;
  emptyMessage?: string;
}) {
  if (rows.length === 0) {
    return (
      <p className="py-8 text-center text-sm text-gray-400">{emptyMessage}</p>
    );
  }
  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
      <table className="min-w-full divide-y divide-gray-200 text-sm dark:divide-gray-800">
        <thead className="bg-gray-50 dark:bg-gray-900">
          <tr>
            {columns.map((c) => (
              <th
                key={c.header}
                className={`px-4 py-2 text-left font-medium text-gray-600 dark:text-gray-300 ${c.className ?? ""}`}
              >
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 bg-white dark:divide-gray-800 dark:bg-gray-950">
          {rows.map((row) => (
            <tr
              key={keyFn(row)}
              className={
                onRowClick
                  ? "cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-900"
                  : ""
              }
              onClick={() => onRowClick?.(row)}
            >
              {columns.map((c) => (
                <td key={c.header} className={`px-4 py-2 ${c.className ?? ""}`}>
                  {c.render(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
