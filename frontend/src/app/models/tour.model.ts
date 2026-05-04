export type TransportType =
  | 'WALK'
  | 'HIKING'
  | 'BIKE'
  | 'MOUNTAIN_BIKE'
  | 'ROAD_BIKE'
  | 'CAR'
  | 'MOTORHOME';

export interface Tour {
  id: number;
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  fromLat: number;
  fromLng: number;
  toLat: number;
  toLng: number;
  transportType: TransportType;
  tourDistance: number | null;
  estimatedTime: number | null;
  tourImagePath: string | null;
  routeGeometry: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TourRequest {
  name: string;
  fromLocation: string;
  toLocation: string;
  fromLat: number;
  fromLng: number;
  toLat: number;
  toLng: number;
  transportType: string;
  description?: string;
}
