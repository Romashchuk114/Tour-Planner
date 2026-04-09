-- Testdaten fuer Tour Planner
-- Testuser Passwort: password123

-- Bestehende Testdaten loeschen
TRUNCATE tour_logs, tours, users RESTART IDENTITY CASCADE;

-- Testuser (Passwort: password123, BCrypt-Hash)
INSERT INTO users (username, email, password) VALUES
  ('testuser', 'test@example.com', '$2a$10$iDkPSaev.cUU1SyOZJ.3JOhtt9lPR44U7avLzAuVw7wkWICu6FlMW');

-- Touren fuer testuser (user_id = 1)
INSERT INTO tours (user_id, name, description, from_location, to_location, transport_type, tour_distance, estimated_time) VALUES
  (1, 'Wiener Stadtwanderung',      'Gemütliche Wanderung durch die Wiener Innenstadt mit Besuch der wichtigsten Sehenswürdigkeiten.', 'Wien Stephansplatz',     'Wien Schönbrunn',      'WALK',             8.5,  120),
  (1, 'Donauradweg Etappe 1',       'Erste Etappe des Donauradwegs von Passau nach Linz entlang der Donau.',                          'Passau',                 'Linz',                 'BIKE',             95.0, 360),
  (1, 'Salzburg - Hallstatt',       'Tagesausflug mit dem Auto von Salzburg nach Hallstatt über die Salzkammergut-Straße.',            'Salzburg Hauptbahnhof',  'Hallstatt',            'CAR',              75.3, 65),
  (1, 'Wiener Öffi-Tour',           'Wien erkunden mit den öffentlichen Verkehrsmitteln.',                                             'Wien Praterstern',       'Wien Zentralfriedhof', 'PUBLIC_TRANSPORT', 12.0, 45),
  (1, 'Graz Schloßberg Wanderung',  'Aufstieg auf den Grazer Schloßberg mit Panoramablick über die Stadt.',                            'Graz Hauptplatz',        'Graz Schloßberg',      'WALK',             2.5,  40);

-- Tour Logs fuer Tour 1: Wiener Stadtwanderung
INSERT INTO tour_logs (tour_id, date_time, comment, difficulty, total_distance, total_time, rating) VALUES
  (1, '2026-03-15 09:00:00', 'Sehr schöne Route, besonders der Burggarten war toll.',                3, 8.2,  115, 5),
  (1, '2026-03-22 10:30:00', 'Etwas regnerisch, aber trotzdem angenehm.',                            2, 8.5,  130, 3),
  (1, '2026-04-01 08:00:00', 'Früh gestartet, kaum Touristen unterwegs. Perfektes Wetter.',          3, 9.0,  125, 5);

-- Tour Logs fuer Tour 2: Donauradweg
INSERT INTO tour_logs (tour_id, date_time, comment, difficulty, total_distance, total_time, rating) VALUES
  (2, '2026-04-05 07:00:00', 'Anstrengende aber wunderschöne Etappe. Gegenwind ab Engelhartszell.',  7, 97.0, 390, 4),
  (2, '2026-04-12 06:30:00', 'Zweiter Versuch, diesmal mit Rückenwind. Deutlich angenehmer.',        5, 95.5, 340, 5);

-- Tour Logs fuer Tour 3: Salzburg - Hallstatt
INSERT INTO tour_logs (tour_id, date_time, comment, difficulty, total_distance, total_time, rating) VALUES
  (3, '2026-03-20 08:00:00', 'Stau bei der Anfahrt, Hallstatt selbst war überfüllt.',                2, 78.0, 90,  3),
  (3, '2026-04-02 07:00:00', 'Früh losgefahren, kaum Verkehr. Hallstatt im Morgenlicht ist magisch.',1, 75.0, 60,  5);

-- Tour Logs fuer Tour 4: Wiener Öffi-Tour
INSERT INTO tour_logs (tour_id, date_time, comment, difficulty, total_distance, total_time, rating) VALUES
  (4, '2026-03-28 14:00:00', 'U-Bahn und Straßenbahn funktionieren einwandfrei.',                    1, 12.5, 50,  4);

-- Tour Logs fuer Tour 5: Graz Schloßberg
INSERT INTO tour_logs (tour_id, date_time, comment, difficulty, total_distance, total_time, rating) VALUES
  (5, '2026-04-06 11:00:00', 'Steiler Aufstieg, aber der Ausblick oben ist es wert.',                5, 2.8,  45,  5),
  (5, '2026-04-08 16:00:00', 'Nachmittags-Wanderung, Sonnenuntergang vom Schloßberg aus.',           4, 2.5,  38,  4);