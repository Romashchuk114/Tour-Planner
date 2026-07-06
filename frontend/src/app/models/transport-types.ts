import { TransportType } from './tour.model';

export interface TransportOption {
  value: TransportType;
  label: string;
}

export const TRANSPORT_OPTIONS: ReadonlyArray<TransportOption> = [
  { value: 'WALK', label: 'Zu Fuß' },
  { value: 'HIKING', label: 'Wandern' },
  { value: 'BIKE', label: 'Fahrrad' },
  { value: 'MOUNTAIN_BIKE', label: 'Mountainbike' },
  { value: 'ROAD_BIKE', label: 'Rennrad' },
  { value: 'CAR', label: 'Auto' },
  { value: 'MOTORHOME', label: 'Wohnmobil / LKW' }
];

export const TRANSPORT_LABEL: Record<TransportType, string> = Object.fromEntries(
  TRANSPORT_OPTIONS.map(o => [o.value, o.label])
) as Record<TransportType, string>;
