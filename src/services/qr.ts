export function extractAutoparkkiUrlFromQr(rawData: string) {
  const data = rawData.trim();
  const urlMatch = data.match(/https?:\/\/[^\s"'<>]+/i);
  const detectedUrl = (urlMatch ? urlMatch[0] : data).trim();

  let parsedUrl: URL;
  try {
    parsedUrl = new URL(detectedUrl);
  } catch {
    return undefined;
  }

  const hostname = parsedUrl.hostname.toLowerCase();
  if (parsedUrl.protocol !== "https:") return undefined;
  if (hostname !== "autoparkki.fi" && !hostname.endsWith(".autoparkki.fi")) return undefined;
  if (!parsedUrl.pathname.toLowerCase().startsWith("/access/")) return undefined;
  return parsedUrl.toString();
}
