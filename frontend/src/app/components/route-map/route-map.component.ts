import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';

@Component({
  selector: 'app-route-map',
  standalone: true,
  imports: [CommonModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div #mapContainer class="route-map"></div>
  `,
  styleUrls: ['./route-map.component.scss']
})
export class RouteMapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() routeGeometry: string | null = null;
  @Input() fromLat!: number;
  @Input() fromLng!: number;
  @Input() toLat!: number;
  @Input() toLng!: number;
  @Input() fromLabel = 'Start';
  @Input() toLabel = 'Ziel';

  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private routeLayer?: L.GeoJSON;
  private fromMarker?: L.Marker;
  private toMarker?: L.Marker;
  private resizeObserver?: ResizeObserver;

  ngAfterViewInit(): void {
    requestAnimationFrame(() => {
      this.initMap();
      this.render();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.map) {
      this.render();
    }
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.map?.remove();
  }

  private initMap(): void {
    const el = this.mapContainer.nativeElement;

    this.map = L.map(el, {
      zoomControl: true,
      scrollWheelZoom: true
    }).setView([47.5, 14.5], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19
    }).addTo(this.map);

    this.resizeObserver = new ResizeObserver(() => {
      this.map?.invalidateSize();
    });
    this.resizeObserver.observe(el);

    setTimeout(() => this.map?.invalidateSize(), 100);
  }

  private render(): void {
    if (!this.map) return;

    if (this.routeLayer) { this.map.removeLayer(this.routeLayer); this.routeLayer = undefined; }
    if (this.fromMarker) { this.map.removeLayer(this.fromMarker); this.fromMarker = undefined; }
    if (this.toMarker) { this.map.removeLayer(this.toMarker); this.toMarker = undefined; }

    const fromIcon = this.makeIcon('#28a745');
    const toIcon = this.makeIcon('#dc3545');

    this.fromMarker = L.marker([this.fromLat, this.fromLng], { icon: fromIcon })
      .addTo(this.map)
      .bindPopup(this.fromLabel);

    this.toMarker = L.marker([this.toLat, this.toLng], { icon: toIcon })
      .addTo(this.map)
      .bindPopup(this.toLabel);

    if (this.routeGeometry) {
      try {
        const geo = JSON.parse(this.routeGeometry);
        this.routeLayer = L.geoJSON(geo, {
          style: { color: '#007bff', weight: 4, opacity: 0.85 }
        }).addTo(this.map);
        this.map.fitBounds(this.routeLayer.getBounds(), { padding: [30, 30] });
      } catch {
        this.fitBoundsToMarkers();
      }
    } else {
      this.fitBoundsToMarkers();
    }
  }

  private fitBoundsToMarkers(): void {
    if (!this.map) return;
    const bounds = L.latLngBounds(
      [this.fromLat, this.fromLng],
      [this.toLat, this.toLng]
    );
    this.map.fitBounds(bounds, { padding: [50, 50] });
  }

  private makeIcon(color: string): L.DivIcon {
    return L.divIcon({
      className: '',
      html: `<div style="
        width: 18px; height: 18px;
        background: ${color};
        border: 3px solid white;
        border-radius: 50%;
        box-shadow: 0 2px 6px rgba(0,0,0,0.4);
      "></div>`,
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });
  }
}
