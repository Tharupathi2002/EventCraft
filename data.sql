-- Initial Sample Data for EventCraft DB (Converted at 1 USD = 300 LKR)

-- Insert Users
INSERT INTO users (name, email, password, role, account_status) VALUES
('Alexander Wright', 'host@eventcraft.com', 'pass123', 'HOST', 'ACTIVE'),
('Grand Luxe Venues', 'contact@grandluxe.com', 'pass123', 'VENUE_PROVIDER', 'ACTIVE'),
('Aura Photography', 'info@auraphoto.com', 'pass123', 'VENDOR', 'ACTIVE');

-- Insert Venues (1 USD = 300 LKR)
INSERT INTO venues (name, street, city, capacity, rate_per_day, description, image_url, rating, status) VALUES
('The Grand Cinnamon Ballroom', '77 Galle Road', 'Colombo 03', 450, 750000.00, 'A luxurious ballroom with panoramic sea views, state-of-the-art lighting, and grand chandeliers.', 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=800&q=80', 4.9, 'AVAILABLE'),
('Lotus Pavilion & Gardens', '12 Parliament Road', 'Kotte', 300, 540000.00, 'Open-air lush tropical garden venue with romantic pavilion suitable for weddings and evening galas.', 'https://images.unsplash.com/photo-1544078751-58fee2d8a03b?auto=format&fit=crop&w=800&q=80', 4.7, 'AVAILABLE'),
('Highland Haven Estate', '45 Nuwara Eliya Rd', 'Nuwara Eliya', 200, 450000.00, 'Colonial style manor nestled in tea hills with indoor fireplace hall and scenic lawn.', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80', 4.8, 'AVAILABLE'),
('Citylights Sky Lounge', '100 Union Place', 'Colombo 02', 150, 360000.00, 'Modern rooftop venue with glass infinity deck, ambient LED setup, ideal for corporate events & cocktail parties.', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80', 4.6, 'AVAILABLE');

-- Insert Vendors (1 USD = 300 LKR)
INSERT INTO vendors (business_name, service_type, contact_info, base_price, description, image_url, rating, status) VALUES
('Aura Cinematic Photography', 'Photography', '+94 77 123 4567 | info@auraphoto.com', 255000.00, 'Award-winning wedding & event photographers specializing in candid storytelling and drone videography.', 'https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=800&q=80', 4.9, 'AVAILABLE'),
('Royal Feast Catering', 'Catering', '+94 71 987 6543 | order@royalfeast.lk', 360000.00, 'Gourmet 5-star catering serving authentic Sri Lankan, Western & Asian fusion buffet spreads.', 'https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=800&q=80', 4.8, 'AVAILABLE'),
('Enchanted Floral & Decor', 'Decor', '+94 76 555 1212 | hello@enchanted.lk', 195000.00, 'Custom themed stage decorations, floral arches, entrance lighting and table centerpieces.', 'https://images.unsplash.com/photo-1478146896981-b80fe463b330?auto=format&fit=crop&w=800&q=80', 4.7, 'AVAILABLE'),
('Sonic Pulse Live Band & DJ', 'Music/DJ', '+94 75 444 3322 | booking@sonicpulse.com', 150000.00, 'Energetic 5-piece live band with pro sound system, intelligent lighting rig and versatile DJ sets.', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80', 4.8, 'AVAILABLE');

-- Insert Bookings (1 USD = 300 LKR)
INSERT INTO bookings (event_id, host_name, host_email, venue_id, vendor_id, booking_type, booking_date, event_date, amount, booking_status, special_requirements) VALUES
(101, 'Sujeewan A.', 'sujeewan@gmail.com', 1, NULL, 'VENUE', '2026-09-10', '2026-11-20', 750000.00, 'CONFIRMED', 'Require 10 VIP banquet tables setup by 3 PM.'),
(102, 'Samantha Perera', 'samantha@yahoo.com', NULL, 1, 'VENDOR', '2026-09-12', '2026-12-05', 255000.00, 'PENDING', 'Drone coverage requested for outdoor garden ceremony.');
