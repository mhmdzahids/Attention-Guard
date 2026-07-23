---
name: Optimistic Commerce
colors:
  surface: '#f9f9fd'
  surface-dim: '#d9dade'
  surface-bright: '#f9f9fd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f7'
  surface-container: '#ededf1'
  surface-container-high: '#e8e8ec'
  surface-container-highest: '#e2e2e6'
  on-surface: '#1a1c1f'
  on-surface-variant: '#424754'
  inverse-surface: '#2f3034'
  inverse-on-surface: '#f0f0f4'
  outline: '#727786'
  outline-variant: '#c2c6d6'
  surface-tint: '#0059c8'
  primary: '#004db0'
  on-primary: '#ffffff'
  primary-container: '#0064e0'
  on-primary-container: '#e6ebff'
  inverse-primary: '#afc6ff'
  secondary: '#5e5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e2e2e2'
  on-secondary-container: '#646464'
  tertiary: '#6f4b00'
  on-tertiary: '#ffffff'
  tertiary-container: '#8f6100'
  on-tertiary-container: '#ffe8ca'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d9e2ff'
  primary-fixed-dim: '#afc6ff'
  on-primary-fixed: '#001944'
  on-primary-fixed-variant: '#004299'
  secondary-fixed: '#e2e2e2'
  secondary-fixed-dim: '#c6c6c6'
  on-secondary-fixed: '#1b1b1b'
  on-secondary-fixed-variant: '#474747'
  tertiary-fixed: '#ffddaf'
  tertiary-fixed-dim: '#ffba42'
  on-tertiary-fixed: '#281800'
  on-tertiary-fixed-variant: '#614000'
  background: '#f9f9fd'
  on-background: '#1a1c1f'
  surface-variant: '#e2e2e6'
  commerce-cobalt: '#0064E0'
  commerce-cobalt-deep: '#0457CB'
  ink-button: '#000000'
  fb-blue: '#1876F2'
  warning-yellow: '#F2A918'
  promo-gold: '#F7B928'
  critical-red: '#E41E3F'
  surface-soft: '#F1F4F7'
  hairline: '#CED0D4'
  hairline-soft: '#DEE3E9'
typography:
  hero-display:
    fontFamily: Be Vietnam Pro
    fontSize: 64px
    fontWeight: '500'
    lineHeight: '1.16'
    letterSpacing: 0px
  display-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 48px
    fontWeight: '500'
    lineHeight: '1.17'
    letterSpacing: 0px
  heading-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 36px
    fontWeight: '500'
    lineHeight: '1.28'
    letterSpacing: 0px
  heading-md:
    fontFamily: Be Vietnam Pro
    fontSize: 28px
    fontWeight: '300'
    lineHeight: '1.21'
    letterSpacing: 0px
  heading-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '500'
    lineHeight: '1.25'
    letterSpacing: 0px
  subtitle-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '700'
    lineHeight: '1.44'
    letterSpacing: 0px
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.50'
    letterSpacing: -0.16px
  body-md-bold:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '700'
    lineHeight: '1.50'
    letterSpacing: -0.16px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.43'
    letterSpacing: -0.14px
  button-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '700'
    lineHeight: '1.43'
    letterSpacing: -0.14px
  caption:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '400'
    lineHeight: '1.33'
    letterSpacing: 0px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xxs: 4px
  xs: 8px
  sm: 10px
  md: 12px
  base: 16px
  lg: 20px
  xl: 24px
  xxl: 32px
  xxxl: 40px
  section: 64px
  hero: 120px
---

## Brand & Style

The design system is built on a **White Canvas** philosophy, where the interface serves as a minimalist, high-clarity stage for hardware photography and immersive storytelling. It balances technical precision with a humanist touch, evoking a brand personality that is confident, accessible, and professional.

The visual style is **Modern Minimalism** with a focus on geometric discipline. It avoids traditional UI "chrome" like heavy shadows or gradients, instead utilizing generous whitespace, high-contrast typography, and distinctive rounding patterns to create a "friendly hardware" aesthetic. The interface is divided into two functional modes:
- **Marketing:** High-contrast (Black/White/Ghost) for brand storytelling and impact.
- **Commerce:** Action-oriented (Cobalt/Blue) for transactional clarity and conversion.

## Colors

The color system is anchored by a stark white background (`#FFFFFF`) to ensure product imagery remains the focal point. 

- **Primary (Commerce Cobalt):** Reserved for high-intent transactional actions like "Add to Cart" or "Checkout."
- **Secondary (Ink Black):** Used for marketing-driven CTAs and high-impact brand moments.
- **Tertiary (Warning Yellow):** Employed for mid-priority alerts and promotional tags to draw attention without signaling an error.
- **Neutrals:** A range of cool-toned grays and deep inks handle the typographic hierarchy and subtle containment.
- **Borders:** Surfaces are defined by thin `1px` hairlines rather than shadows, maintaining a flat, architectural feel.

## Typography

This design system utilizes a variable font approach (mapped here to **Be Vietnam Pro** for its humanist geometric qualities). 

A key typographic signature is the use of a **300-weight (Light)** for editorial subheads (`heading-md`), which provides a sophisticated rhythmic break between bold display titles and functional body text. 

For high-density body copy, apply negative tracking (approx. -1% or -0.15px) to maintain the "tight" typographic texture characteristic of the brand. Headers should utilize stylistic alternates where available to emphasize geometric forms.

## Layout & Spacing

The layout is governed by a **4px base increment**, with **8px (xs)** serving as the dominant rhythm for internal component alignment.

### Grid & Margins
- **Desktop:** 12-column fluid grid with 24px gutters. Use `xxl` (32px) or `xxxl` (40px) for container padding to mirror the rounding of the cards.
- **Mobile:** 4-column grid with 16px margins. 
- **Section Breaks:** Use `section` (64px) for standard vertical rhythm and `hero` (120px) for major marketing transitions.

Spacing should be used generously to maintain the "White Canvas" feel—negative space is as important as the content itself.

## Elevation & Depth

Depth is conveyed through **Tonal Layering** and **High-Contrast Outlines** rather than traditional drop shadows.

- **Flat (Level 0):** Used for 90% of the UI. Cards and containers use a `1px` border in `hairline-soft`.
- **Raised (Level 1):** Subtle elevation is restricted to active pill indicators, using a minimal `1px` spread shadow.
- **Sticky (Level 2):** Applied only to persistent elements like the Product Detail Page (PDP) purchase rail or mobile navigation bars, using a tight, dark shadow to denote overlay status.
- **Legibility Overlays:** When text appears over photography, use a `10-12%` dark or light tint overlay to ensure contrast without obscuring the image.

## Shapes

The design system is defined by extreme, intentional rounding that echoes physical product industrial design.

- **Pill Shapes (100px/Full):** Used for all interactive elements including buttons, search bars, and badges.
- **Generous Cards (32px / xxxl):** Photographic product cards and marketing strips must use this radius to create a friendly, approachable container.
- **Medium Elements (16px / xl):** Feature cards and thumbnails.
- **Small Elements (8px / lg):** Form inputs and radio containers.

Consistency in rounding is critical: if it is a button, it must be a pill. If it is a container, it must follow the hierarchy of the 32/16/8 scale.

## Components

### Buttons
- **Primary (Commerce):** Pill-shaped, Cobalt Blue background, White text.
- **Primary (Marketing):** Pill-shaped, Black background, White text.
- **Ghost:** Pill-shaped, 2px border (Black or Cobalt), transparent background.
- **Sizing:** Fixed height with generous horizontal padding (approx. 30px) to maintain the pill proportions.

### Input Fields & Controls
- **Inputs:** 8px (`lg`) corner radius with `1px` hairline border. Focused state uses a 2px `fb-blue` border.
- **Radio/Checkbox:** Circular for radio, slightly rounded for checkbox. Selection states use `fb-blue`.
- **SKU Pickers:** 32px circular swatches for colors; 8px rounded tiles for sizes. Selection is indicated by a 2px offset ring.

### Cards
- **Feature Cards:** Use 32px (`xxxl`) rounding. Often borderless if containing full-bleed photography.
- **Standard Cards:** 16px (`xl`) rounding with a `1px` soft hairline border.

### Chips & Badges
- **Pill Tabs:** Always fully rounded. Active state uses a white surface with a subtle Level 1 shadow; inactive state uses a `surface-soft` background.