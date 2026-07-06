import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from './components/header/header';
import { Notification } from './components/notification/notification';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Notification],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
