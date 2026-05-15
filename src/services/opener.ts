import type { GarageProfile, PlateProfile } from "../types/profiles";

export type OpenDoorResult = {
  ok: boolean;
  message: string;
  status?: number;
  finalUrl?: string;
  requestUrl?: string;
  responseSnippet?: string;
};

type ParsedControl = {
  tag: "input" | "button" | "textarea";
  name?: string;
  value: string;
  type?: string;
  id?: string;
  placeholder?: string;
  ariaLabel?: string;
  text?: string;
};

type ParsedForm = {
  action?: string;
  method: "GET" | "POST";
  controls: ParsedControl[];
  csrfToken?: string;
};

function decodeHtml(input: string) {
  return input
    .replace(/&nbsp;/gi, " ")
    .replace(/&amp;/gi, "&")
    .replace(/&lt;/gi, "<")
    .replace(/&gt;/gi, ">")
    .replace(/&#39;/gi, "'")
    .replace(/&quot;/gi, '"')
    .trim();
}

function stripHtml(input: string) {
  return decodeHtml(
    input
      .replace(/<script[\s\S]*?<\/script>/gi, "")
      .replace(/<style[\s\S]*?<\/style>/gi, "")
      .replace(/<[^>]+>/g, " ")
      .replace(/\s+/g, " ")
  );
}

function parseAttributes(tag: string) {
  const attrs: Record<string, string> = {};
  const attrRegex =
    /([:\w-]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>`]+)))?/g;

  let match: RegExpExecArray | null;
  while ((match = attrRegex.exec(tag))) {
    const name = match[1]?.toLowerCase();
    if (!name) continue;
    const value = match[2] ?? match[3] ?? match[4] ?? "";
    attrs[name] = decodeHtml(value);
  }

  return attrs;
}

function extractCsrfToken(html: string) {
  const metaMatch = html.match(
    /<meta[^>]+name=["']csrf-token["'][^>]+content=["']([^"']+)["'][^>]*>/i
  );
  if (metaMatch?.[1]) return decodeHtml(metaMatch[1]);

  const inputMatch = html.match(
    /<input[^>]+name=["'](?:_token|csrf|csrf_token|_csrf|authenticity_token)["'][^>]+value=["']([^"']+)["'][^>]*>/i
  );
  if (inputMatch?.[1]) return decodeHtml(inputMatch[1]);

  return undefined;
}

function parseControls(formHtml: string): ParsedControl[] {
  const controls: ParsedControl[] = [];

  const inputRegex = /<input\b[^>]*>/gi;
  let inputMatch: RegExpExecArray | null;
  while ((inputMatch = inputRegex.exec(formHtml))) {
    const attrs = parseAttributes(inputMatch[0]);
    const type = (attrs.type || "text").toLowerCase();

    if (type === "button" || type === "image" || type === "file" || type === "reset") {
      continue;
    }

    if ((type === "checkbox" || type === "radio") && !("checked" in attrs)) {
      continue;
    }

    controls.push({
      tag: "input",
      name: attrs.name,
      value: attrs.value || "",
      type,
      id: attrs.id,
      placeholder: attrs.placeholder,
      ariaLabel: attrs["aria-label"],
    });
  }

  const textareaRegex = /<textarea\b([^>]*)>([\s\S]*?)<\/textarea>/gi;
  let textareaMatch: RegExpExecArray | null;
  while ((textareaMatch = textareaRegex.exec(formHtml))) {
    const attrs = parseAttributes(textareaMatch[1] || "");
    controls.push({
      tag: "textarea",
      name: attrs.name,
      value: decodeHtml(textareaMatch[2] || ""),
      id: attrs.id,
      placeholder: attrs.placeholder,
      ariaLabel: attrs["aria-label"],
    });
  }

  const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
  let buttonMatch: RegExpExecArray | null;
  while ((buttonMatch = buttonRegex.exec(formHtml))) {
    const attrs = parseAttributes(buttonMatch[1] || "");
    const type = (attrs.type || "submit").toLowerCase();

    if (type !== "submit") continue;

    controls.push({
      tag: "button",
      name: attrs.name,
      value: attrs.value || stripHtml(buttonMatch[2] || ""),
      type,
      text: stripHtml(buttonMatch[2] || ""),
    });
  }

  return controls;
}

function parseForms(html: string): ParsedForm[] {
  const forms: ParsedForm[] = [];
  const formRegex = /<form\b([^>]*)>([\s\S]*?)<\/form>/gi;

  let match: RegExpExecArray | null;
  while ((match = formRegex.exec(html))) {
    const attrs = parseAttributes(match[1] || "");
    const formHtml = match[2] || "";
    const method = (attrs.method || "GET").toUpperCase() === "POST" ? "POST" : "GET";

    forms.push({
      action: attrs.action,
      method,
      controls: parseControls(formHtml),
      csrfToken: extractCsrfToken(formHtml),
    });
  }

  return forms;
}

function controlIdentity(control: ParsedControl) {
  return [
    control.name,
    control.id,
    control.placeholder,
    control.ariaLabel,
    control.text,
  ]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
}

function looksLikePlateField(control: ParsedControl) {
  if (!control.name) return false;

  const identity = controlIdentity(control);

  return (
    /plate/.test(identity) ||
    /license/.test(identity) ||
    /licence/.test(identity) ||
    /registration/.test(identity) ||
    /register/.test(identity) ||
    /regno/.test(identity) ||
    /reg_no/.test(identity) ||
    /rekister/.test(identity) ||
    /rekisterinumero/.test(identity) ||
    /vehicle/.test(identity) ||
    /car/.test(identity)
  );
}

function isUserTextControl(control: ParsedControl) {
  const type = (control.type || "text").toLowerCase();

  return (
    control.name &&
    control.tag !== "button" &&
    type !== "hidden" &&
    type !== "submit" &&
    type !== "password"
  );
}

function chooseOpenForm(forms: ParsedForm[]) {
  if (forms.length === 0) return undefined;

  const plateForm = forms.find((form) => form.controls.some(looksLikePlateField));
  if (plateForm) return plateForm;

  const textInputForm = forms.find((form) => form.controls.some(isUserTextControl));
  if (textInputForm) return textInputForm;

  return forms[0];
}

function buildSubmission(form: ParsedForm, plateNumber: string) {
  const params = new URLSearchParams();

  for (const control of form.controls) {
    if (!control.name) continue;

    const type = (control.type || "text").toLowerCase();
    if (type === "submit" || control.tag === "button") {
      continue;
    }

    params.set(control.name, control.value || "");
  }

  const plateField =
    form.controls.find(looksLikePlateField) ||
    form.controls.find(isUserTextControl);

  if (!plateField?.name) {
    throw new Error("Could not find the license plate input field on the access page.");
  }

  params.set(plateField.name, plateNumber.trim().toUpperCase());

  const submitControl =
    form.controls.find(
      (control) =>
        control.name &&
        (control.type === "submit" || control.tag === "button") &&
        /avaa|open|door|ovi|submit/i.test(`${control.value} ${control.text}`)
    ) ||
    form.controls.find(
      (control) => control.name && (control.type === "submit" || control.tag === "button")
    );

  if (submitControl?.name) {
    params.set(submitControl.name, submitControl.value || submitControl.text || "1");
  }

  return params;
}

function responseLooksSuccessful(responseText: string) {
  const text = stripHtml(responseText).toLowerCase();

  if (
    /virhe/.test(text) ||
    /error/.test(text) ||
    /failed/.test(text) ||
    /invalid/.test(text) ||
    /väär/.test(text)
  ) {
    return false;
  }

  if (
    /avattu/.test(text) ||
    /ovi avautuu/.test(text) ||
    /ovi on avattu/.test(text) ||
    /opened/.test(text) ||
    /success/.test(text)
  ) {
    return true;
  }

  // Some door pages may return the same page with HTTP 200 after submission.
  // Treat HTTP success as "request sent" rather than guaranteed physical opening.
  return undefined;
}

function absoluteUrl(action: string | undefined, baseUrl: string) {
  if (!action) return baseUrl;
  return new URL(action, baseUrl).toString();
}

export async function mockOpenDoor(
  garage: GarageProfile,
  plate: PlateProfile
): Promise<string> {
  await new Promise((resolve) => setTimeout(resolve, 900));
  return `Mock success: ${garage.name} · ${plate.plateNumber}`;
}

export async function realOpenDoor(
  garage: GarageProfile,
  plate: PlateProfile
): Promise<OpenDoorResult> {
  const accessUrl = garage.accessUrl.trim();
  const plateNumber = plate.plateNumber.trim().toUpperCase();

  if (!accessUrl.startsWith("https://")) {
    throw new Error("Garage access URL must start with https://.");
  }

  if (!plateNumber) {
    throw new Error("License plate is missing.");
  }

  const getResponse = await fetch(accessUrl, {
    method: "GET",
    headers: {
      Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    },
    credentials: "include",
  });

  const getHtml = await getResponse.text();
  const forms = parseForms(getHtml);
  const form = chooseOpenForm(forms);

  if (!form) {
    throw new Error("No form was found on the Autoparkki access page.");
  }

  const submitUrl = absoluteUrl(form.action, getResponse.url || accessUrl);
  const params = buildSubmission(form, plateNumber);
  const csrfToken = form.csrfToken || extractCsrfToken(getHtml);

  let submitResponse: Response;

  if (form.method === "GET") {
    const url = new URL(submitUrl);
    params.forEach((value, key) => url.searchParams.set(key, value));

    submitResponse = await fetch(url.toString(), {
      method: "GET",
      headers: {
        Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        Referer: getResponse.url || accessUrl,
        ...(csrfToken ? { "X-CSRF-TOKEN": csrfToken } : {}),
      },
      credentials: "include",
    });
  } else {
    submitResponse = await fetch(submitUrl, {
      method: "POST",
      headers: {
        Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: getResponse.url || accessUrl,
        ...(csrfToken ? { "X-CSRF-TOKEN": csrfToken } : {}),
      },
      body: params.toString(),
      credentials: "include",
    });
  }

  const responseText = await submitResponse.text();
  const plainSnippet = stripHtml(responseText).slice(0, 500);
  const semanticSuccess = responseLooksSuccessful(responseText);
  const ok = submitResponse.ok && semanticSuccess !== false;

  const message =
    semanticSuccess === true
      ? `Door request accepted for ${plateNumber}.`
      : submitResponse.ok
        ? `Door request sent for ${plateNumber}. Please verify the door.`
        : `Door request failed with HTTP ${submitResponse.status}.`;

  return {
    ok,
    message,
    status: submitResponse.status,
    finalUrl: submitResponse.url,
    requestUrl: submitUrl,
    responseSnippet: plainSnippet,
  };
}
