# Design System Strategy: The Nocturnal Gourmet

## 1. Overview & Creative North Star
The North Star for this design system is **"The Nocturnal Gourmet."** We are transcending the traditional "fast food" aesthetic to create a digital experience that feels like a high-end, late-night lounge. This system moves away from the noisy, cluttered layouts of the industry and embraces **Organic Precision**. 

We achieve a premium feel through intentional asymmetry—where large-scale typography breaks traditional grid boundaries—and "Atmospheric Depth," where the UI feels like layered glass floating in a dark, infinite space. This is not just an interface; it is a cinematic presentation of flavor.

---

## 2. Colors: Tonal Depth & The "No-Line" Rule
The palette is rooted in a deep, obsidian foundation, using red and yellow not as "loud" primary colors, but as sophisticated accents and glowing focal points.

### Color Tokens
- **Background/Surface:** `#131313` (The base of the experience).
- **Primary (Red):** `primary_container` (`#DB0007`) — Used for high-impact brand moments.
- **Secondary (Yellow):** `secondary` (`#FFC72C`) — Used for precise call-to-actions and interactive highlights.
- **Surface Tiers:** Use `surface_container_lowest` (`#0E0E0E`) through `surface_container_highest` (`#353534`) to define hierarchy.

### The "No-Line" Rule
**Explicit Instruction:** Do not use 1px solid borders to define sections. This is a "No-Line" system. 
- **Separation through Shift:** To separate a menu category from the header, shift the background from `surface` to `surface_container_low`.
- **Nesting Hierarchy:** Treat the UI as physical layers. A product card (`surface_container_high`) should sit atop a category section (`surface_container_low`), which sits on the main `background`. This creates a sophisticated, natural shadow-less depth.

### The "Glass & Gradient" Rule
Standard flat buttons are forbidden for primary actions. 
- **Signature Textures:** For main Hero CTAs, use a subtle linear gradient transitioning from `primary_container` (`#DB0007`) to a deeper tone to add "soul."
- **Glassmorphism:** Navigation bars and floating action buttons must use `surface_bright` at 60% opacity with a `20px` backdrop-blur. This allows the vibrant food photography to "bleed" through the UI, softening the edges of the digital environment.

---

## 3. Typography: Editorial Authority
We utilize a dual-font system to balance high-end editorial flair with functional readability.

- **Display & Headlines (Epilogue):** This is our "voice." **Epilogue** is bold and expansive. Use `display-lg` (3.5rem) with tight letter-spacing for product names. Don't be afraid to let a headline overlap a product image slightly to create a layered, "magazine" feel.
- **Body & Titles (Manrope):** This is our "utility." **Manrope** provides a technical, clean contrast to the expressive headlines. It ensures that even at `body-sm` (0.75rem), nutritional info and prices are crystal clear.

**Hierarchy Note:** Always lead with size and weight before color. A `headline-lg` in `on_surface` is more authoritative than a small label in red.

---

## 4. Elevation & Depth: Tonal Layering
In "The Nocturnal Gourmet," depth is felt, not seen. We avoid the "floating card" look of 2014 material design.

- **The Layering Principle:** Depth is achieved by stacking the `surface-container` tiers. 
    *   *Base:* `surface` (#131313)
    *   *Section:* `surface_container_low` (#1C1B1B)
    *   *Component:* `surface_container_high` (#2A2A2A)
- **Ambient Shadows:** If an element must float (e.g., a modal or cart drawer), use a shadow with a `48px` blur at 8% opacity. The shadow color should be tinted with `on_surface` to mimic the way light dies in a dark room.
- **The "Ghost Border":** If accessibility requires a container edge, use the `outline_variant` token at **15% opacity**. This creates a "suggestion" of an edge rather than a hard boundary.

---

## 5. Components: Precision & Smoothness

### Buttons
- **Primary:** Gradient fill (`primary_container` to `#930003`), `roundness-md` (0.375rem), `title-sm` (Manrope Bold).
- **Secondary:** `surface_container_highest` fill with `secondary` (#FFC72C) text. No border.
- **Tertiary:** Text-only in `secondary` with a subtle glow on hover.

### Cards & Lists
- **Rule:** Forbid divider lines. 
- **Execution:** Use `40px` of vertical whitespace (from the spacing scale) to separate items. For lists, use a alternating tonal shift (e.g., Row 1 is `surface`, Row 2 is `surface_container_low`).
- **Product Cards:** Use a subtle `xl` (0.75rem) corner radius. The image should occupy the top 70% of the card, bleeding to the edges.

### Input Fields
- **State:** Resting state is `surface_container_highest`. 
- **Focus:** Transition the background to `surface_bright` and add a `2px` glow using the `secondary_fixed` token. Do not use a hard-edged stroke.

### Specialized Component: The "Glow Chip"
For dietary tags (e.g., "Vegan," "Spicy"), use a transparent chip with a `secondary` text color and a very soft `secondary` outer glow. This mimics the neon signage of a high-end eatery.

---

## 6. Do’s and Don'ts

### Do:
*   **Do** use asymmetrical layouts where the product image is larger than the container.
*   **Do** leverage `surface_container_lowest` for the deepest parts of the UI (like background footers).
*   **Do** use high-contrast typography scales (e.g., a huge `display-lg` price next to a tiny `label-sm` unit).
*   **Do** use "Sophisticated Yellow" (#FFC72C) sparingly—it should be a beacon, not a bucket of paint.

### Don’t:
*   **Don't** use 100% white (#FFFFFF) for text. Use `on_surface` (#E5E2E1) to reduce eye strain and maintain the premium dark-mode aesthetic.
*   **Don't** use standard 1px borders or dividers. They shatter the "liquid glass" illusion.
*   **Don't** use default "Drop Shadows." Use Tonal Layering or Ambient Shadows only.
*   **Don't** crowd the interface. If a screen feels busy, increase the whitespace by 2x. High-end design requires room to breathe.