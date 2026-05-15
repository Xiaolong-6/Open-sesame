export type DebugFetchResult = {
  ok: boolean;
  requestedUrl: string;
  finalUrl?: string;
  status?: number;
  statusText?: string;
  contentType?: string | null;
  title?: string;
  snippet?: string;
  error?: string;
};

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

function extractTitle(html: string) {
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

export async function debugFetchAccessPage(accessUrl: string): Promise<DebugFetchResult> {
  const requestedUrl = accessUrl.trim();

  if (!requestedUrl.startsWith("https://")) {
    return {
      ok: false,
      requestedUrl,
      error: "Access URL must start with https://",
    };
  }

  try {
    const response = await fetch(requestedUrl, {
      method: "GET",
      headers: {
        Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
      },
    });

    const text = await response.text();
    const title = extractTitle(text);
    const plainText = stripHtml(text);

    return {
      ok: response.ok,
      requestedUrl,
      finalUrl: response.url,
      status: response.status,
      statusText: response.statusText,
      contentType: response.headers.get("content-type"),
      title,
      snippet: plainText.slice(0, 1200),
    };
  } catch (error) {
    return {
      ok: false,
      requestedUrl,
      error:
        error instanceof Error
          ? error.message
          : "Unknown error while fetching the access page.",
    };
  }
}
