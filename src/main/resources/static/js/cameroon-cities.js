/**
 * SMOMA — reference list of Cameroonian cities used for mission-step locations.
 * The four ART Regional Delegations (Douala, Yaoundé, Garoua, Bamenda) and the other
 * regional chief-towns (where ART operates spectrum-monitoring / contact posts) are flagged.
 * Bound to <datalist> so a step location can be picked from the list or still typed freely
 * (e.g. an international city for an external mission).
 */
const CAMEROON_CITIES = [
    // --- ART Regional Delegations -------------------------------------------------
    { name: "Yaoundé",      region: "Centre",         art: "Délégation Régionale ART (Siège)" },
    { name: "Douala",       region: "Littoral",       art: "Délégation Régionale ART" },
    { name: "Garoua",       region: "Nord",           art: "Délégation Régionale ART" },
    { name: "Bamenda",      region: "Nord-Ouest",     art: "Délégation Régionale ART" },
    // --- Other regional chief-towns (antenne / station ART) ---------------------
    { name: "Bafoussam",    region: "Ouest",          art: "Chef-lieu de région — antenne ART" },
    { name: "Maroua",       region: "Extrême-Nord",   art: "Chef-lieu de région — antenne ART" },
    { name: "Ngaoundéré",   region: "Adamaoua",       art: "Chef-lieu de région — antenne ART" },
    { name: "Bertoua",      region: "Est",            art: "Chef-lieu de région — antenne ART" },
    { name: "Ebolowa",      region: "Sud",            art: "Chef-lieu de région — antenne ART" },
    { name: "Buea",         region: "Sud-Ouest",      art: "Chef-lieu de région — antenne ART" },
    // --- Strategic telecom infrastructure sites --------------------------------
    { name: "Kribi",        region: "Sud",            art: "Station d'atterrissement des câbles sous-marins" },
    { name: "Limbe",        region: "Sud-Ouest" },
    { name: "Edéa",         region: "Littoral" },
    // --- Other major cities ---------------------------------------------------
    { name: "Nkongsamba",   region: "Littoral" },
    { name: "Kumba",        region: "Sud-Ouest" },
    { name: "Foumban",      region: "Ouest" },
    { name: "Dschang",      region: "Ouest" },
    { name: "Mbouda",       region: "Ouest" },
    { name: "Bafang",       region: "Ouest" },
    { name: "Bangangté",    region: "Ouest" },
    { name: "Mbalmayo",     region: "Centre" },
    { name: "Bafia",        region: "Centre" },
    { name: "Obala",        region: "Centre" },
    { name: "Eséka",        region: "Centre" },
    { name: "Nanga-Eboko",  region: "Centre" },
    { name: "Akonolinga",   region: "Centre" },
    { name: "Sangmélima",   region: "Sud" },
    { name: "Ambam",        region: "Sud" },
    { name: "Abong-Mbang",  region: "Est" },
    { name: "Batouri",      region: "Est" },
    { name: "Yokadouma",    region: "Est" },
    { name: "Meiganga",     region: "Adamaoua" },
    { name: "Tibati",       region: "Adamaoua" },
    { name: "Banyo",        region: "Adamaoua" },
    { name: "Tignère",      region: "Adamaoua" },
    { name: "Kousséri",     region: "Extrême-Nord" },
    { name: "Yagoua",       region: "Extrême-Nord" },
    { name: "Kaélé",        region: "Extrême-Nord" },
    { name: "Mora",         region: "Extrême-Nord" },
    { name: "Mokolo",       region: "Extrême-Nord" },
    { name: "Guider",       region: "Nord" },
    { name: "Figuil",       region: "Nord" },
    { name: "Poli",         region: "Nord" },
    { name: "Tcholliré",    region: "Nord" },
    { name: "Kumbo",        region: "Nord-Ouest" },
    { name: "Wum",          region: "Nord-Ouest" },
    { name: "Ndop",         region: "Nord-Ouest" },
    { name: "Fundong",      region: "Nord-Ouest" },
    { name: "Mamfe",        region: "Sud-Ouest" },
    { name: "Tiko",         region: "Sud-Ouest" },
    { name: "Mutengene",    region: "Sud-Ouest" },
    { name: "Mbanga",       region: "Littoral" },
    { name: "Loum",         region: "Littoral" },
    { name: "Manjo",        region: "Littoral" },
    { name: "Melong",       region: "Littoral" },
    { name: "Mbandjock",    region: "Centre" }
];

/** Fill a <datalist> element with the city list (value = city, label = ART role or region). */
function populateCityDatalist(datalistId) {
    const dl = document.getElementById(datalistId);
    if (!dl) return;
    const seen = new Set();
    dl.innerHTML = CAMEROON_CITIES
        .filter(c => c && c.name && !c.skip && !seen.has(c.name) && seen.add(c.name))
        .map(c => `<option value="${c.name}">${c.art ? c.art : c.region}</option>`)
        .join('');
}

/**
 * Returns the <option>/<optgroup> markup for a city <select>, grouped as
 * "Délégations / Antennes ART" then "Autres grandes villes", with the current value
 * pre-selected (kept even if it is a city outside Cameroon), plus an "Autre ville" entry.
 */
function cityOptionsHtml(selectedValue) {
    const sel = (selectedValue || '').trim();
    const seen = new Set();
    const list = CAMEROON_CITIES.filter(c => c && c.name && !c.skip && !seen.has(c.name) && seen.add(c.name));
    const opt = c => `<option value="${c.name}"${c.name === sel ? ' selected' : ''}>`
        + `${c.name}${c.art ? ' — ' + c.art : ''}</option>`;
    const art = list.filter(c => c.art);
    const other = list.filter(c => !c.art);

    let html = `<option value=""${sel ? '' : ' selected'}>— Sélectionner une ville —</option>`;
    html += `<optgroup label="Délégations / Antennes ART">${art.map(opt).join('')}</optgroup>`;
    html += `<optgroup label="Autres grandes villes du Cameroun">${other.map(opt).join('')}</optgroup>`;
    if (sel && !list.some(c => c.name === sel)) {
        html += `<optgroup label="Hors Cameroun / autre"><option value="${sel}" selected>${sel}</option></optgroup>`;
    }
    html += `<option value="__other__">➕ Autre ville / ville hors liste…</option>`;
    return html;
}
