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

  if (ogTitle?.[1]) {
    return stripHtml(ogTitle[1]);
  }

  const title = html.match(/<title[^>]*>([\s\S]*?)<\/title>/i);
  if (title?.[1]) {
    return stripHtml(title[1]);
  }

  const h1 = html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i);
  if (h1?.[1]) {
    return stripHtml(h1[1]);
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

    if (title) {
      // Remove generic browser/app words while keeping location-specific text.
      const cleaned = title
        .replace(/\s*[-|–—]\s*Autoparkki\s*$/i, "")
        .replace(/^Autoparkki\s*[-|–—]\s*/i, "")
        .trim();

      if (cleaned.length >= 3) {
        return cleaned;
      }

      return title;
    }
  } catch {
    // Web builds may fail here because of CORS. Native builds should usually work.
  }

  return fallbackNameFromUrl(cleanUrl);
}
