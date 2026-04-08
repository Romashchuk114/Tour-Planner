export interface TourLog {
  id: number;
  tourId: number;
  dateTime: string;
  comment: string;
  difficulty: number;
  totalDistance: number;
  totalTime: number;
  rating: number;
  createdAt: string;
  updatedAt: string;
}

export interface TourLogRequest {
  dateTime: string;
  comment?: string;
  difficulty: number;
  totalDistance: number;
  totalTime: number;
  rating: number;
}
