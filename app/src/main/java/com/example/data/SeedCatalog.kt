package com.example.data

import com.example.data.model.CustomerReview
import com.example.data.model.GalleryPhoto
import com.example.data.model.ServiceCategory
import com.example.data.model.SpecialOffer
import com.example.data.model.StudioConfig
import com.example.data.model.StudioService

object SeedCatalog {

    val defaultConfig = StudioConfig(
        id = "main",
        businessName = "Studio Pixy",
        tagline = "Luxury Portraits, Frames & Same-Day Photo Lab",
        whatsappNumber = "+919876543210",
        phoneNumber = "+919876543210",
        address = "Shop 12, Main Market Road, Near City Center",
        email = "hello@studiopixy.in",
        openingHours = "Mon - Sun: 9:30 AM - 9:00 PM",
        mapsQuery = "Studio Pixy Photography Studio",
        specialOfferTitle = "Wedding & Frame Combo Offer",
        specialOfferDetails = "Flat 15% OFF on 12x18 & 16x20 Photo Frames + Free Matte Lamination"
    )

    val defaultServices: List<StudioService> = listOf(
        // A. PHOTO FRAMES
        StudioService(
            id = "frame_4x6",
            name = "4x6 Photo Frame",
            hindiName = "4x6 फोटो फ्रेम (टेबल/वॉल)",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 150L,
            unitLabel = "Per Frame",
            description = "Compact matte-black & gold trim frame with HD glossy print included.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 1L
        ),
        StudioService(
            id = "frame_5x7",
            name = "5x7 Photo Frame",
            hindiName = "5x7 फोटो फ्रेम",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 220L,
            unitLabel = "Per Frame",
            description = "Ideal desk & gift frame with crystal acrylic protection and free print.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 2L
        ),
        StudioService(
            id = "frame_8x10",
            name = "8x10 Photo Frame",
            hindiName = "8x10 फोटो फ्रेम (पॉपुलर)",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 450L,
            unitLabel = "Per Frame",
            description = "Popular portrait frame with luxury wooden moulding and anti-glare finish.",
            presetKey = "frames",
            available = true,
            featured = true,
            sortOrder = 3L
        ),
        StudioService(
            id = "frame_10x12",
            name = "10x12 Photo Frame",
            hindiName = "10x12 फोटो फ्रेम",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 650L,
            unitLabel = "Per Frame",
            description = "Rich walnut & gold bezel wall frame suitable for family portraits.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 4L
        ),
        StudioService(
            id = "frame_12x18",
            name = "12x18 Photo Frame",
            hindiName = "12x18 बड़ा फोटो फ्रेम",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 1100L,
            unitLabel = "Per Frame",
            description = "Statement living-room wall frame with velvet-backed matte mounting.",
            presetKey = "frames",
            available = true,
            featured = true,
            sortOrder = 5L
        ),
        StudioService(
            id = "frame_16x20",
            name = "16x20 Photo Frame",
            hindiName = "16x20 वेडिंग फोटो फ्रेम",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 1850L,
            unitLabel = "Per Frame",
            description = "Gallery-grade large frame crafted for wedding & anniversary portraits.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 6L
        ),
        StudioService(
            id = "frame_20x24",
            name = "20x24 Photo Frame",
            hindiName = "20x24 रॉयल वॉल फ्रेम",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 2800L,
            unitLabel = "Per Frame",
            description = "Grand royal canvas-ready frame with UV-resistant lamination.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 7L
        ),
        StudioService(
            id = "frame_custom",
            name = "Custom Size Frames",
            hindiName = "कस्टम साइज फ्रेम (मनचाहा आकार)",
            category = ServiceCategory.PHOTO_FRAMES.name,
            priceInr = 500L,
            unitLabel = "Starting Price",
            description = "Tailor-made collage, LED, or oversized frames built to exact dimensions.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 8L
        ),

        // B. PASSPORT PHOTOGRAPHY
        StudioService(
            id = "passport_std",
            name = "Passport Size Photo",
            hindiName = "पासपोर्ट साइज फोटो (8 कॉपी)",
            category = ServiceCategory.PASSPORT_PHOTOGRAPHY.name,
            priceInr = 100L,
            unitLabel = "Set of 8",
            description = "Studio-lit portrait with clean white/blue backdrop, printed in 15 mins.",
            presetKey = "portrait",
            available = true,
            featured = true,
            sortOrder = 9L
        ),
        StudioService(
            id = "passport_visa",
            name = "Visa Photo",
            hindiName = "वीज़ा फोटो (सभी देश)",
            category = ServiceCategory.PASSPORT_PHOTOGRAPHY.name,
            priceInr = 200L,
            unitLabel = "Set of 6",
            description = "US, UK, Schengen, Canada & Gulf biometric specification compliant.",
            presetKey = "portrait",
            available = true,
            featured = true,
            sortOrder = 10L
        ),
        StudioService(
            id = "passport_stamp",
            name = "Stamp Size Photo",
            hindiName = "स्टाम्प साइज फोटो (16 कॉपी)",
            category = ServiceCategory.PASSPORT_PHOTOGRAPHY.name,
            priceInr = 80L,
            unitLabel = "Set of 16",
            description = "Crisp mini stamp prints ideal for forms, IDs, and school admissions.",
            presetKey = "portrait",
            available = true,
            featured = false,
            sortOrder = 11L
        ),
        StudioService(
            id = "passport_digital",
            name = "Digital Passport Photo",
            hindiName = "डिजिटल पासपोर्ट फोटो (सॉफ्ट कॉपी)",
            category = ServiceCategory.PASSPORT_PHOTOGRAPHY.name,
            priceInr = 120L,
            unitLabel = "HD File + 4 Prints",
            description = "Exact KB/pixel resized JPEG delivered on WhatsApp & Email + 4 prints.",
            presetKey = "portrait",
            available = true,
            featured = false,
            sortOrder = 12L
        ),
        StudioService(
            id = "passport_urgent",
            name = "Urgent Passport Photo",
            hindiName = "तुरंत पासपोर्ट फोटो (10 मिनट)",
            category = ServiceCategory.PASSPORT_PHOTOGRAPHY.name,
            priceInr = 180L,
            unitLabel = "Express Set of 12",
            description = "Priority express shoot, retouching & precision cut within 10 minutes.",
            presetKey = "portrait",
            available = true,
            featured = true,
            sortOrder = 13L
        ),

        // C. PHOTOGRAPHY SERVICES
        StudioService(
            id = "photo_wedding",
            name = "Wedding Photography",
            hindiName = "शादी फोटोग्राफी पैकेज",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 25000L,
            unitLabel = "Full Package",
            description = "Candid + traditional coverage, 40-sheet luxury album & edited gallery.",
            presetKey = "wedding",
            available = true,
            featured = true,
            sortOrder = 14L
        ),
        StudioService(
            id = "photo_birthday",
            name = "Birthday Photography",
            hindiName = "जन्मदिन पार्टी फोटोग्राफी",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 4500L,
            unitLabel = "Per Event",
            description = "3-hour coverage with candid family moments & color-graded digital gallery.",
            presetKey = "portrait",
            available = true,
            featured = false,
            sortOrder = 15L
        ),
        StudioService(
            id = "photo_prewedding",
            name = "Pre-Wedding Photography",
            hindiName = "प्री-वेडिंग आउटडोर शूट",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 15000L,
            unitLabel = "Full Day Shoot",
            description = "Scenic location shoot with creative lighting, outfit changes & teaser reel.",
            presetKey = "wedding",
            available = true,
            featured = true,
            sortOrder = 16L
        ),
        StudioService(
            id = "photo_event",
            name = "Event Photography",
            hindiName = "इवेंट एवं फंक्शन फोटोग्राफी",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 6500L,
            unitLabel = "Per Day",
            description = "Corporate events, engagements, housewarmings & stage celebrations.",
            presetKey = "studio",
            available = true,
            featured = false,
            sortOrder = 17L
        ),
        StudioService(
            id = "photo_outdoor",
            name = "Outdoor Photography",
            hindiName = "आउटडोर पोर्ट्रेट शूट",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 3500L,
            unitLabel = "2-Hour Session",
            description = "Golden-hour natural light portraits for couples, maternity & portfolios.",
            presetKey = "wedding",
            available = true,
            featured = false,
            sortOrder = 18L
        ),
        StudioService(
            id = "photo_family",
            name = "Family Photography",
            hindiName = "फैमिली स्टूडियो पोर्ट्रेट",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 2500L,
            unitLabel = "Studio Session",
            description = "Comfortable air-conditioned studio session with 1 framed 12x18 print.",
            presetKey = "portrait",
            available = true,
            featured = false,
            sortOrder = 19L
        ),
        StudioService(
            id = "photo_cinematic_video",
            name = "Cinematic Video Shooting",
            hindiName = "सिनेमैटिक 4K वीडियो शूटिंग",
            category = ServiceCategory.PHOTOGRAPHY_SERVICES.name,
            priceInr = 18000L,
            unitLabel = "4K Cinema Edit",
            description = "Gimbal 4K filming, drone highlights & emotional soundtrack film.",
            presetKey = "studio",
            available = true,
            featured = true,
            sortOrder = 20L
        ),

        // D. PHOTO PRINTING
        StudioService(
            id = "print_4x6",
            name = "4x6 Photo Printing",
            hindiName = "4x6 फोटो प्रिंट (ग्लॉसी/मैट)",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 20L,
            unitLabel = "Per Print",
            description = "Archival Fujifilm/Kodak silver-halide prints with true-to-life skin tones.",
            presetKey = "studio",
            available = true,
            featured = false,
            sortOrder = 21L
        ),
        StudioService(
            id = "print_5x7",
            name = "5x7 Photo Printing",
            hindiName = "5x7 फोटो प्रिंट",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 40L,
            unitLabel = "Per Print",
            description = "Rich contrast matte or glossy prints for albums and table frames.",
            presetKey = "studio",
            available = true,
            featured = false,
            sortOrder = 22L
        ),
        StudioService(
            id = "print_8x10",
            name = "8x10 Photo Printing",
            hindiName = "8x10 फोटो प्रिंट",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 90L,
            unitLabel = "Per Print",
            description = "Sharp portrait printing ready for standard 8x10 framing.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 23L
        ),
        StudioService(
            id = "print_a4",
            name = "A4 Photo Printing",
            hindiName = "A4 हाई-रेजोल्यूशन प्रिंट",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 140L,
            unitLabel = "Per Sheet",
            description = "Borderless A4 fine-art photo paper print with optional lamination.",
            presetKey = "studio",
            available = true,
            featured = false,
            sortOrder = 24L
        ),
        StudioService(
            id = "print_a3",
            name = "A3 Photo Printing",
            hindiName = "A3 पोस्टर फोटो प्रिंट",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 280L,
            unitLabel = "Per Sheet",
            description = "Large format poster-quality print with vivid colour calibration.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 25L
        ),
        StudioService(
            id = "print_enlargement",
            name = "Photo Enlargement",
            hindiName = "फोटो बड़ा करें (एन्लार्जमेंट)",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 350L,
            unitLabel = "Per Print",
            description = "AI-assisted upscaling & sharpening so old small photos print crystal clear.",
            presetKey = "frames",
            available = true,
            featured = false,
            sortOrder = 26L
        ),
        StudioService(
            id = "print_restoration",
            name = "Old Photo Restoration",
            hindiName = "पुरानी फटी फोटो की मरम्मत",
            category = ServiceCategory.PHOTO_PRINTING.name,
            priceInr = 450L,
            unitLabel = "Per Photo",
            description = "Scratch removal, tear repair, colour revival & fresh archival print.",
            presetKey = "portrait",
            available = true,
            featured = true,
            sortOrder = 27L
        )
    )

    val defaultOffers: List<SpecialOffer> = listOf(
        SpecialOffer(
            id = "offer_festive_frame",
            title = "15% OFF on Wedding & Family Frames",
            badge = "BESTSELLER",
            description = "Order 12x18 or 16x20 wooden gold frames with free anti-glare lamination.",
            promoCode = "PIXYFRAME15",
            active = true
        ),
        SpecialOffer(
            id = "offer_passport_combo",
            title = "Urgent Passport + Digital Copy Combo",
            badge = "10 MIN EXPRESS",
            description = "Get 12 hard-copy prints plus resized WhatsApp/Email file at ₹220 only.",
            promoCode = "FASTPASS",
            active = true
        ),
        SpecialOffer(
            id = "offer_wedding_early",
            title = "Free Pre-Wedding Reel on Full Wedding Booking",
            badge = "WEDDING SPECIAL",
            description = "Book Wedding Photography + 4K Cinematic Video and receive a 60s teaser reel free.",
            promoCode = "ROYALWEDDING",
            active = true
        )
    )

    val defaultReviews: List<CustomerReview> = listOf(
        CustomerReview(
            id = "rev_1",
            authorName = "Rohit Verma",
            rating = 5L,
            comment = "Got my US Visa & Passport photos in 10 minutes! Lighting and print clarity are studio-perfect.",
            serviceTag = "Visa & Passport Photo"
        ),
        CustomerReview(
            id = "rev_2",
            authorName = "Priya & Ankit",
            rating = 5L,
            comment = "Studio Pixy covered our wedding and made our 16x20 gold frames. Every guest loved the photos!",
            serviceTag = "Wedding Photography"
        ),
        CustomerReview(
            id = "rev_3",
            authorName = "Sunita Devi",
            rating = 5L,
            comment = "They restored my parents' 40-year-old torn black & white photo into a gorgeous framed print.",
            serviceTag = "Old Photo Restoration"
        )
    )

    val galleryPhotos: List<GalleryPhoto> = listOf(
        GalleryPhoto("g1", "Luxury Portrait Frame Wall", "लग्ज़री फ्रेम कलेक्शन", "Photo Frames", "frames"),
        GalleryPhoto("g2", "Golden Hour Royal Wedding", "रॉयल वेडिंग फोटोग्राफी", "Wedding Shoot", "wedding"),
        GalleryPhoto("g3", "Pro Studio Softbox Lighting", "स्टूडियो पोर्ट्रेट लाइटिंग", "Studio Portraits", "portrait"),
        GalleryPhoto("g4", "Studio Pixy Flagship Lab", "स्टूडियो पिक्सी लैब", "Flagship Studio", "studio")
    )
}
