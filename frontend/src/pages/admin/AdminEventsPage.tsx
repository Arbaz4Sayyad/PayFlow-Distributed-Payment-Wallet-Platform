import React from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { MOCK_OUTBOX_EVENTS } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';

export const AdminEventsPage: React.FC = () => {
  return (
    <div className="space-y-4">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-base font-bold uppercase tracking-wider text-slate-100 font-mono">
          Transactional Outbox & Event Stream
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Guaranteed at-least-once event delivery across Kafka topics and Dead Letter Topics (DLT).
        </p>
      </div>

      <div className="bg-slate-950 border border-slate-800 rounded-md overflow-hidden font-mono text-xs">
        <Table className="bg-slate-950 text-slate-200">
          <TableHeader className="bg-slate-900 border-slate-800 text-slate-400">
            <TableRow>
              <TableHead>Event ID</TableHead>
              <TableHead>Event Type</TableHead>
              <TableHead>Aggregate Type / ID</TableHead>
              <TableHead>Payload Preview</TableHead>
              <TableHead className="text-center">Status</TableHead>
              <TableHead className="text-right">Published At</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-slate-800">
            {MOCK_OUTBOX_EVENTS.map((evt) => (
              <TableRow key={evt.id} className="hover:bg-slate-900/60">
                <TableCell className="text-slate-400">{evt.id}</TableCell>
                <TableCell className="text-emerald-400 font-bold">{evt.eventType}</TableCell>
                <TableCell>
                  <span className="text-slate-300">{evt.aggregateType}</span>
                  <span className="text-slate-500 text-[10px] block">{evt.aggregateId}</span>
                </TableCell>
                <TableCell className="max-w-xs truncate text-slate-400 text-[11px]">
                  {evt.payload}
                </TableCell>
                <TableCell className="text-center">
                  <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800 px-1.5 py-0.5 rounded">
                    {evt.status}
                  </span>
                </TableCell>
                <TableCell className="text-right text-slate-500 text-[11px]">
                  {formatDateTime(evt.createdAt)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
