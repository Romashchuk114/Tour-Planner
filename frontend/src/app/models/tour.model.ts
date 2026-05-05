export type TransportType =
  | 'WALK'
  | 'HIKING'
  | 'BIKE'
  | 'MOUNTAIN_BIKE'
  | 'ROAD_BIKE'
  | 'CAR'
  | 'MOTORHOME';

export interface Stage {
  orderIndex: number;
  transportType: TransportType;
  endName: string;
  endLat: number;
  endLng: number;
  distance: number;
  duration: number;
  geometryGeoJson: string | null;
}

export interface Tour {
  id: number;
  name: string;
  description: string;
  fromName: string;
  fromLat: number;
  fromLng: number;
  totalDistance: number;
  totalDuration: number;
  tourImagePath: string | null;
  stages: Stage[];
  createdAt: string;
  updatedAt: string;
}

export interface TourRequest {
  name: string;
  description?: string;
  fromName: string;
  fromLat: number;
  fromLng: number;
  stages: { transportType: string; endName: string; endLat: number; endLng: number }[];
}
