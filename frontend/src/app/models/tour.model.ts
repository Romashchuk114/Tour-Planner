export type TransportType = 'CAR' | 'WALK' | 'PUBLIC_TRANSPORT' | 'BIKE';

export interface Tour {
  id: number;
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: TransportType;
  tourDistance: number | null;
  estimatedTime: number | null;
  tourImagePath: string | null;
  logCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface TourRequest {
  name: string;
  fromLocation: string;
  toLocation: string;
  transportType: string;
  description?: string;
  tourDistance?: number;
  estimatedTime?: number;
}
