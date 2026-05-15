function stripHtml(input: string) {
  return input
    .replace(/<script[\s\S]*?<\/script>/gi, "")
    .replace(/<style[\s\S]*?<\/style>/gi, "")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/gi, " ")
    .replace(/&amp;/gi, "&")
    .replace(/&lt;/gi, "<")
    .replace(/&gt;/gi, ">")
    .replace(/&#39;/gi, "'")
    .replace(/&quot;/gi, '"')
    .replace(/\s+/g, " ")
    .trim();
}

function extractTitleFromHtml(html: string) {
  const ogTitle = html.match(
    /<meta[^>]+property=["']og:title["'][^>]+content=["']([^"']+)["'][^>]*>/i
  );

  if (ogTitle?.[1]) return stripHtml(ogTitle[1]);

  const title = html.match(/<title[^>]*>([\s\S]*?)<\/title>/i);
  if (title?.[1]) return stripHtml(title[1]);

  const h1 = html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i);
  if (h1?.[1]) return stripHtml(h1[1]);

  return undefined;
}

function extractLocationFromPlainText(plainText: string, title?: string) {
  let text = plainText.replace(/\s+/g, " ").trim();

  if (title) {
    text = text.replace(title, "").trim();
  }

  // Autoparkki access pages commonly contain:
  // "P-Luoteisrinne Finnoonsilta Syötä rekisterinumerosi Avaa ovi ..."
  const beforePlatePrompt = text.split(/Syötä rekisterinumerosi/i)[0]?.trim();
  if (beforePlatePrompt && beforePlatePrompt.length >= 3) {
    const cleaned = beforePlatePrompt
      .replace(/^[-–—|\s]+/, "")
      .replace(/\s+/g, " ")
      .trim();

    if (cleaned.length >= 3 && cleaned.length <= 80) {
      return cleaned;
    }
  }

  const parkingName = text.match(/\bP-[A-Za-zÅÄÖåäö0-9][A-Za-zÅÄÖåäö0-9\- ]{2,80}/);
  if (parkingName?.[0]) {
    return parkingName[0].trim();
  }

  return undefined;
}

function fallbackNameFromUrl(accessUrl: string) {
  try {
    const parsed = new URL(accessUrl);
    const token = parsed.pathname.split("/").filter(Boolean).pop();

    if (token && token.length >= 8) {
      return `Autoparkki ${token.slice(0, 8)}`;
    }

    return parsed.hostname.replace(/^dc\./, "");
  } catch {
    return undefined;
  }
}

export async function suggestDoorNameFromAccessUrl(accessUrl: string) {
  const cleanUrl = accessUrl.trim();

  if (!cleanUrl.startsWith("https://")) {
    return undefined;
  }

  try {
    const response = await fetch(cleanUrl, {
      method: "GET",
      headers: {
        Accept: "text/html,application/xhtml+xml",
      },
    });

    const html = await response.text();
    const title = extractTitleFromHtml(html);
    const plainText = stripHtml(html);
    const locationName = extractLocationFromPlainText(plainText, title);

    if (locationName) {
      return locationName;
    }

    if (title) {
      const cleaned = title
        .replace(/\s*[-|–—]\s*Autoparkki\s*$/i, "")
        .replace(/^Autoparkki\s*[-|–—]\s*/i, "")
        .trim();

      return cleaned.length >= 3 ? cleaned : title;
    }
  } catch {
    // Web builds may fail here because of CORS. Native builds should usually work.
  }

  return fallbackNameFromUrl(cleanUrl);
}
