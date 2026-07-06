import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  ViewEncapsulation,
  effect,
  input
} from '@angular/core';
import * as L from 'leaflet';
import { SEGMENT_COLORS } from './map-colors';

export interface MapPoint {
  lat: number;
  lng: number;
  label: string;
  kind: 'start' | 'waypoint' | 'end';
  index?: number;
}

export interface MapSegment {
  geometryGeoJson: string | null;
  transportType: string;
}

@Component({
  selector: 'app-route-map',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  template: `<div #mapContainer class="route-map"></div>`,
  styleUrls: ['./route-map.component.scss']
})
export class RouteMapComponent implements AfterViewInit, OnDestroy {
  points = input<MapPoint[]>([]);
  segments = input<MapSegment[]>([]);

  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private routeLayers: L.GeoJSON[] = [];
  private markers: L.Marker[] = [];
  private resizeObserver?: ResizeObserver;
  private lastRenderKey: string | null = null;

  constructor() {
    effect(() => {
      const pts = this.points();
      const segs = this.segments();
      if (this.map) {
        this.render(pts, segs);
      }
    });
  }

  ngAfterViewInit(): void {
    requestAnimationFrame(() => {
      this.initMap();
      this.render(this.points(), this.segments());
    });
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.map?.remove();
  }

  private initMap(): void {
    const el = this.mapContainer.nativeElement;
    this.map = L.map(el, { zoomControl: true, scrollWheelZoom: true })
      .setView([47.5, 14.5], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19
    }).addTo(this.map);

    this.resizeObserver = new ResizeObserver(() => this.map?.invalidateSize());
    this.resizeObserver.observe(el);
    setTimeout(() => this.map?.invalidateSize(), 100);
  }

  private render(points: MapPoint[], segments: MapSegment[]): void {
    if (!this.map) return;

    // Skip if structurally identical to last render (computed-Signals erzeugen neue Array-Identität bei jeder Re-Evaluation)
    const key = this.computeKey(points, segments);
    if (key === this.lastRenderKey) return;
    this.lastRenderKey = key;

    this.routeLayers.forEach(l => this.map!.removeLayer(l));
    this.routeLayers = [];
    this.markers.forEach(m => this.map!.removeLayer(m));
    this.markers = [];

    if (!points.length) return;

    points.forEach(p => {
      const marker = L.marker([p.lat, p.lng], { icon: this.makeIcon(p.kind, p.index) })
        .addTo(this.map!)
        .bindPopup(p.label);
      this.markers.push(marker);
    });

    const bounds = L.latLngBounds([]);
    points.forEach(p => bounds.extend([p.lat, p.lng]));

    segments.forEach((seg, i) => {
      if (!seg.geometryGeoJson) return;
      try {
        const geo = JSON.parse(seg.geometryGeoJson);
        const layer = L.geoJSON(geo, {
          style: { color: SEGMENT_COLORS[i % SEGMENT_COLORS.length], weight: 4, opacity: 0.85 }
        }).addTo(this.map!);
        layer.bindPopup(`Etappe ${i + 1}: ${seg.transportType}`);
        this.routeLayers.push(layer);
        bounds.extend(layer.getBounds());
      } catch {
        // skip broken geometry
      }
    });

    if (bounds.isValid()) {
      this.map.fitBounds(bounds, { padding: [40, 40] });
    }
  }

  private computeKey(points: MapPoint[], segments: MapSegment[]): string {
    const p = points.map(pt => `${pt.kind}:${pt.lat},${pt.lng}`).join('|');
    const s = segments.map(seg => `${seg.transportType}:${seg.geometryGeoJson?.length ?? 0}`).join('|');
    return `${p}#${s}`;
  }

  private makeIcon(kind: 'start' | 'waypoint' | 'end', index?: number): L.DivIcon {
    if (kind === 'waypoint') {
      return L.divIcon({
        className: 'map-icon map-icon--waypoint',
        html: `<span>${index ?? 0}</span>`,
        iconSize: [22, 22],
        iconAnchor: [11, 11]
      });
    }
    return L.divIcon({
      className: `map-icon map-icon--${kind}`,
      html: '',
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });
  }
}
