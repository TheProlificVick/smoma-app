
let stepCount = 0;

function addItineraryStep(departure = '', destination = '', travelDate = '', transportMode = 'VEHICULE_SERVICE') {
    stepCount++;
    const container = document.getElementById('itineraryStepsContainer');
    
    const stepRow = document.createElement('div');
    stepRow.className = 'row g-2 mb-3 align-items-center step-row border-start border-3 border-primary ps-2 bg-white p-2 rounded shadow-sm';
    stepRow.id = `stepRow_${stepCount}`;

    stepRow.innerHTML = `
        <div class="col-md-1 fw-bold text-secondary text-center">
            <span class="badge bg-primary">Étape ${stepCount}</span>
        </div>
        <div class="col-md-3">
            <label class="form-label small fw-semibold mb-1">Ville de Départ</label>
            <input type="text" name="steps[${stepCount - 1}].departureCity" class="form-control form-control-sm departure-input" value="${departure}" placeholder="Ex: Yaoundé" required oninput="updateSummary()">
        </div>
        <div class="col-md-3">
            <label class="form-label small fw-semibold mb-1">Ville de Destination</label>
            <input type="text" name="steps[${stepCount - 1}].destinationCity" class="form-control form-control-sm destination-input" value="${destination}" placeholder="Ex: Douala" required oninput="updateSummary()">
        </div>
        <div class="col-md-2">
            <label class="form-label small fw-semibold mb-1">Date du Trajet</label>
            <input type="date" name="steps[${stepCount - 1}].travelDate" class="form-control form-control-sm travel-date-input" value="${travelDate}" required>
        </div>
        <div class="col-md-2">
            <label class="form-label small fw-semibold mb-1">Moyen de Transport</label>
            <select name="steps[${stepCount - 1}].transportMode" class="form-select form-select-sm">
                <option value="VEHICULE_SERVICE" ${transportMode === 'VEHICULE_SERVICE' ? 'selected' : ''}>Véhicule Service</option>
                <option value="AVION" ${transportMode === 'AVION' ? 'selected' : ''}>Avion (Camair-Co)</option>
                <option value="TRAIN" ${transportMode === 'TRAIN' ? 'selected' : ''}>Train (Camrail)</option>
                <option value="VEHICULE_PERSONNEL" ${transportMode === 'VEHICULE_PERSONNEL' ? 'selected' : ''}>Véhicule Personnel</option>
            </select>
        </div>
        <div class="col-md-1 text-center">
            <button type="button" class="btn btn-outline-danger btn-sm mt-3" onclick="removeItineraryStep(${stepCount})">
                <i class="fa-solid fa-trash"></i>
            </button>
        </div>
    `;

    container.appendChild(stepRow);
    updateSummary();
}

function removeItineraryStep(id) {
    const row = document.getElementById(`stepRow_${id}`);
    if (row) {
        row.remove();
        reindexSteps();
        updateSummary();
    }
}

function reindexSteps() {
    const rows = document.querySelectorAll('.step-row');
    stepCount = 0;
    rows.forEach((row, index) => {
        stepCount++;
        row.id = `stepRow_${stepCount}`;
        row.querySelector('.badge').innerText = `Étape ${stepCount}`;
        row.querySelector('.departure-input').name = `steps[${index}].departureCity`;
        row.querySelector('.destination-input').name = `steps[${index}].destinationCity`;
        row.querySelector('.travel-date-input').name = `steps[${index}].travelDate`;
        row.querySelector('select').name = `steps[${index}].transportMode`;
    });
}

function updateSummary() {
    const deps = document.querySelectorAll('.departure-input');
    const dests = document.querySelectorAll('.destination-input');
    let summaryParts = [];

    deps.forEach((dep, i) => {
        const depVal = dep.value.trim();
        const destVal = dests[i] ? dests[i].value.trim() : '';
        if (depVal && destVal) {
            summaryParts.push(`${depVal} ➔ ${destVal}`);
        }
    });

    const summaryInput = document.getElementById('itinerarySummary');
    if (summaryInput) {
        summaryInput.value = summaryParts.join(' | ');
    }
}

// Pre-populate with default first leg (Yaoundé Headquarters)
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('itineraryStepsContainer') && stepCount === 0) {
        addItineraryStep('Yaoundé', '', '', 'VEHICULE_SERVICE');
    }
});