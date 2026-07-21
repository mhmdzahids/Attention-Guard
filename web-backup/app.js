// Attention Guard - Core Application Logic & State Management

// App State
const state = {
    // Passive behavior signals
    signals: {
        sessionDuration: 2.5,        // hours (range 0 to 8.0)
        launchFrequency: 12,        // launches/day (range 0 to 30)
        scrollVelocity: 142,        // px/sec (range 0 to 250)
        skipFrequency: 64,          // % (range 0 to 100)
        switchFreq: 8.2,            // switches/hour (range 0 to 20.0)
        foregroundLatency: 1.5,     // sec (range 0 to 5.0)
        nightRatio: 0.75            // ratio (range 0 to 1.0)
    },
    // Calculated values
    apiScore: 0.52,
    riskTier: 'moderate', // 'low', 'moderate', 'high'
    prevRiskTier: 'moderate',
    
    // UI settings & navigation
    activeTab: 'today',
    monitoring: {
        sessionDynamics: true,
        microBehaviors: true,
        taskSwitching: true,
        temporalDistribution: true
    },
    localStorageOnly: true,
    quietHours: true,
    nudgeFrequency: 'balanced', // 'minimal', 'balanced', 'active'
    
    // Historical alerts triggered
    alerts: [
        {
            id: 1,
            timestamp: new Date(Date.now() - 3600000 * 2).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + " Today",
            title: "Late-Night Scrolling Detected",
            description: "You've spent 40% more time on short-form videos after midnight this week.",
            apiScore: 0.52,
            riskTier: "moderate"
        },
        {
            id: 2,
            timestamp: "10:15 AM Yesterday",
            title: "High Task-Switching",
            description: "Task switching frequency exceeded normal baseline by 35% during work hours.",
            apiScore: 0.58,
            riskTier: "moderate"
        },
        {
            id: 3,
            timestamp: "08:30 PM 2 Days Ago",
            title: "Sustained Low-Risk State",
            description: "Attention Performance Indicator maintained stable cognitive load.",
            apiScore: 0.24,
            riskTier: "low"
        }
    ]
};

// SVG Chart points cache
const mockChartPoints = [
    { time: "12 PM", score: 0.25 },
    { time: "3 PM", score: 0.38 },
    { time: "6 PM", score: 0.45 },
    { time: "9 PM", score: 0.58 },
    { time: "12 AM", score: 0.72 },
    { time: "3 AM", score: 0.78 },
    { time: "6 AM", score: 0.32 },
    { time: "9 AM", score: 0.28 }
];

// Document Load Listener
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

// App Initialization
function initApp() {
    // 1. Initial State Calculation
    calculateAPI();
    
    // 2. Set Up Event Listeners for Bottom Nav Navigation
    setupNavigation();

    // 3. Set Up Interaction Controls (Sliders & Switches in Settings)
    setupInteractionControls();
    
    // 4. Modal Setup
    setupModalButtons();

    // 5. Initial Draw & Render
    updateUI();
    
    // 6. Live Clock on status bar
    startClock();
}

// Live Clock for Phone Mockup Status Bar
function startClock() {
    const clockEl = document.getElementById('phone-clock');
    if (!clockEl) return;
    
    const updateTime = () => {
        const now = new Date();
        clockEl.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    };
    
    updateTime();
    setInterval(updateTime, 60000);
}

// State Calculation Logic
function calculateAPI() {
    const s = state.signals;
    
    // Normalization functions (N(x))
    const N_session = Math.min(1.0, Math.max(0.0, s.sessionDuration / 8.0));
    const N_scroll = Math.min(1.0, Math.max(0.0, s.scrollVelocity / 250.0));
    const N_switch = Math.min(1.0, Math.max(0.0, s.switchFreq / 20.0));
    const N_night = Math.min(1.0, Math.max(0.0, s.nightRatio));
    
    // Formula: API = 0.30*N(session_duration) + 0.20*N(scroll_velocity) + 0.30*N(switch_freq) + 0.20*N(night_ratio)
    const rawScore = (0.30 * N_session) + (0.20 * N_scroll) + (0.30 * N_switch) + (0.20 * N_night);
    state.apiScore = parseFloat(rawScore.toFixed(2));
    
    // Map to 3-tier risk system
    state.prevRiskTier = state.riskTier;
    if (state.apiScore < 0.35) {
        state.riskTier = 'low';
    } else if (state.apiScore < 0.65) {
        state.riskTier = 'moderate';
    } else {
        state.riskTier = 'high';
    }
    
    // Trigger Alerts if risk tier increases or escalates
    checkForTriggers();
}

// Checker to verify if we need to open alert modal
function checkForTriggers() {
    if (state.riskTier === state.prevRiskTier) return;
    
    // Log transition
    console.log(`Risk transitioned from ${state.prevRiskTier} to ${state.riskTier}`);
    
    if (state.riskTier === 'high') {
        // High Risk Overlay trigger
        triggerHighRiskOverlay();
    } else if (state.riskTier === 'moderate' && state.prevRiskTier === 'low') {
        // Moderate Risk Modal trigger
        triggerModerateRiskModal();
    }
}

// Trigger Moderate Risk Nudge Modal
function triggerModerateRiskModal() {
    const modal = document.getElementById('behavioral-alert-modal');
    if (!modal) return;
    
    // Push alert history record
    const timestamp = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + " Today";
    state.alerts.unshift({
        id: Date.now(),
        timestamp: timestamp,
        title: "Late-Night Scroll Alert",
        description: "Interaction micro-behaviors indicate fast skipping under moderate cognitive load.",
        apiScore: state.apiScore,
        riskTier: "moderate"
    });
    
    // Update contents in modal
    document.getElementById('modal-nudge-score').textContent = `API Score: ${state.apiScore}`;
    
    // Show Modal
    modal.classList.add('active');
    document.getElementById('app-viewport-container').style.overflow = 'hidden';
}

// Trigger High Risk Prevention Plan Overlay
function triggerHighRiskOverlay() {
    const overlay = document.getElementById('prevention-plan-overlay');
    if (!overlay) return;
    
    // Push alert history record
    const timestamp = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + " Today";
    state.alerts.unshift({
        id: Date.now(),
        timestamp: timestamp,
        title: "High Attention Risk Detected",
        description: "Severe pattern fragmentation detected across all passive behavior sensors.",
        apiScore: state.apiScore,
        riskTier: "high"
    });
    
    // Update scores in overlay modal
    document.getElementById('overlay-title-badge').textContent = `High Risk Detected (API: ${state.apiScore})`;
    document.getElementById('overlay-banner-text').textContent = `High Attention Risk (API: ${state.apiScore})`;
    
    // Show Overlay Modal
    overlay.classList.add('active');
    document.getElementById('app-viewport-container').style.overflow = 'hidden';
}

// Set Up Tab Navigation
function setupNavigation() {
    const navButtons = document.querySelectorAll('.bottom-nav-btn');
    
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            if (targetTab) {
                switchTab(targetTab);
            }
        });
    });
}

// Switch between screens
function switchTab(tabName) {
    state.activeTab = tabName;
    
    // Update bottom nav highlighting
    const navButtons = document.querySelectorAll('.bottom-nav-btn');
    navButtons.forEach(btn => {
        const btnTab = btn.getAttribute('data-tab');
        const icon = btn.querySelector('.material-symbols-outlined');
        
        if (btnTab === tabName) {
            btn.classList.add('text-commerce-cobalt', 'bg-surface-container', 'scale-95');
            btn.classList.remove('text-secondary', 'opacity-60');
            if (icon) icon.setAttribute('data-weight', 'fill');
        } else {
            btn.classList.remove('text-commerce-cobalt', 'bg-surface-container', 'scale-95');
            btn.classList.add('text-secondary', 'opacity-60');
            if (icon) icon.removeAttribute('data-weight');
        }
    });
    
    // Switch Screen Visibility
    const screens = document.querySelectorAll('.app-screen');
    screens.forEach(screen => {
        if (screen.id === `screen-${tabName}`) {
            screen.classList.add('active');
        } else {
            screen.classList.remove('active');
        }
    });
    
    // Tab Specific Actions
    if (tabName === 'insights') {
        animateInsightsBars();
        drawInsightsChart();
    } else if (tabName === 'alerts') {
        renderAlertHistory();
    } else if (tabName === 'today') {
        animateDashboardGauge();
    }
}

// Setup Interaction Controls (sliders & settings)
function setupInteractionControls() {
    // Get sliders
    const sliders = [
        { id: 'slider-session', key: 'sessionDuration', labelId: 'val-session', suffix: ' hrs' },
        { id: 'slider-launches', key: 'launchFrequency', labelId: 'val-launches', suffix: ' launches' },
        { id: 'slider-velocity', key: 'scrollVelocity', labelId: 'val-velocity', suffix: ' px/s' },
        { id: 'slider-skips', key: 'skipFrequency', labelId: 'val-skips', suffix: '%' },
        { id: 'slider-switches', key: 'switchFreq', labelId: 'val-switches', suffix: '/hr' },
        { id: 'slider-latency', key: 'foregroundLatency', labelId: 'val-latency', suffix: 's' },
        { id: 'slider-night', key: 'nightRatio', labelId: 'val-night', suffix: '%', multiplier: 100 }
    ];
    
    sliders.forEach(s => {
        const sliderEl = document.getElementById(s.id);
        const labelEl = document.getElementById(s.labelId);
        if (!sliderEl) return;
        
        // Initialize value
        let initialVal = state.signals[s.key];
        if (s.multiplier) {
            sliderEl.value = initialVal * s.multiplier;
        } else {
            sliderEl.value = initialVal;
        }
        
        // Input event (realtime updating)
        sliderEl.addEventListener('input', (e) => {
            let val = parseFloat(e.target.value);
            if (s.multiplier) {
                state.signals[s.key] = val / s.multiplier;
                if (labelEl) labelEl.textContent = `${Math.round(val)}${s.suffix}`;
            } else {
                state.signals[s.key] = val;
                if (labelEl) labelEl.textContent = `${val}${s.suffix}`;
            }
            
            // Recalculate values and update UI
            calculateAPI();
            updateUI();
        });
    });
    
    // Toggle switch simulation
    const toggleButtons = document.querySelectorAll('.toggle-switch-btn');
    toggleButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const toggleKey = btn.getAttribute('data-toggle');
            if (btn.classList.contains('active')) {
                btn.classList.remove('active');
                btn.classList.add('inactive');
                if (toggleKey) state.monitoring[toggleKey] = false;
            } else {
                btn.classList.remove('inactive');
                btn.classList.add('active');
                if (toggleKey) state.monitoring[toggleKey] = true;
            }
        });
    });
}

// Modal setups
function setupModalButtons() {
    // Got it button in Moderate Alert Modal
    const btnGotIt = document.getElementById('btn-close-nudge');
    const moderateModal = document.getElementById('behavioral-alert-modal');
    if (btnGotIt && moderateModal) {
        btnGotIt.addEventListener('click', () => {
            moderateModal.classList.remove('active');
            document.getElementById('app-viewport-container').style.overflowY = 'auto';
        });
    }
    
    // Close button (X) on High Risk Overlay Modal
    const btnCloseOverlay = document.getElementById('btn-close-overlay');
    const highOverlay = document.getElementById('prevention-plan-overlay');
    if (btnCloseOverlay && highOverlay) {
        btnCloseOverlay.addEventListener('click', () => {
            highOverlay.classList.remove('active');
            document.getElementById('app-viewport-container').style.overflowY = 'auto';
        });
    }

    // Modal action buttons: Activate Prevention Plan
    const btnActivatePlanOverlay = document.getElementById('btn-activate-plan-overlay');
    if (btnActivatePlanOverlay) {
        btnActivatePlanOverlay.addEventListener('click', () => {
            // Cobalt CTA Action - trigger prevention plan activation
            if (highOverlay) highOverlay.classList.remove('active');
            document.getElementById('app-viewport-container').style.overflowY = 'auto';
            // Switch to Meditate screen
            switchTab('meditate');
        });
    }
    
    // Modal remind me later button
    const btnRemindLater = document.getElementById('btn-remind-later');
    if (btnRemindLater) {
        btnRemindLater.addEventListener('click', () => {
            if (highOverlay) highOverlay.classList.remove('active');
            document.getElementById('app-viewport-container').style.overflowY = 'auto';
        });
    }

    // Wire up "View Prevention Plan" button inside Insights tab footer
    const btnViewPlanFromInsights = document.getElementById('btn-view-plan-insights');
    if (btnViewPlanFromInsights) {
        btnViewPlanFromInsights.addEventListener('click', () => {
            switchTab('meditate');
        });
    }
    
    // Wire up "Activate Prevention Plan" on Meditate screen (Cobalt Blue CTA)
    const btnActivateMeditate = document.getElementById('btn-activate-meditate');
    if (btnActivateMeditate) {
        btnActivateMeditate.addEventListener('click', () => {
            alert('Prevention Plan activated successfully! Quiet hours and Micro-breaks are now active.');
        });
    }
}

// Update UI Values Across All Screens
function updateUI() {
    // 1. Dashboard (Today Tab)
    const apiScoreEl = document.getElementById('dash-api-score');
    if (apiScoreEl) apiScoreEl.textContent = state.apiScore.toFixed(2);
    
    // Update Risk Badge Color & Text
    const riskBadge = document.getElementById('dash-risk-badge');
    if (riskBadge) {
        // Clear old badge classes
        riskBadge.className = "font-button-md text-button-md px-lg py-xxs rounded-full inline-block text-white";
        
        if (state.riskTier === 'low') {
            riskBadge.classList.add('badge-low');
            riskBadge.textContent = "Low Risk";
        } else if (state.riskTier === 'moderate') {
            riskBadge.classList.add('badge-moderate');
            riskBadge.textContent = "Moderate Risk";
        } else {
            riskBadge.classList.add('badge-high');
            riskBadge.textContent = "High Risk";
        }
    }
    
    // Update Gauge Ring dashoffset
    animateDashboardGauge();
    
    // Update Dashboard Metrics Cards
    const dashSessionVal = document.getElementById('dash-session-val');
    if (dashSessionVal) dashSessionVal.textContent = `${state.signals.sessionDuration.toFixed(1)} hrs`;
    
    const dashScrollVal = document.getElementById('dash-scroll-val');
    if (dashScrollVal) {
        dashScrollVal.textContent = state.signals.scrollVelocity > 160 ? "Fast" : (state.signals.scrollVelocity < 80 ? "Slow" : "Normal");
    }
    
    const dashSwitchesVal = document.getElementById('dash-switches-val');
    if (dashSwitchesVal) dashSwitchesVal.textContent = `${Math.round(state.signals.switchFreq)}/hr`;
    
    const dashNightVal = document.getElementById('dash-night-val');
    if (dashNightVal) dashNightVal.textContent = `${Math.round(state.signals.nightRatio * 100)}%`;
    
    // Update Today's Focus Suggestion
    const focusRecommendation = document.getElementById('focus-recommendation');
    if (focusRecommendation) {
        if (state.riskTier === 'low') {
            focusRecommendation.textContent = "Your digital attention patterns are healthy. Maintain current habits.";
        } else if (state.riskTier === 'moderate') {
            focusRecommendation.textContent = "Mild scrolling acceleration detected. A quick 5-min breathing session is advised.";
        } else {
            focusRecommendation.textContent = "High task switching and post-midnight usage. Enabling Lockout mode recommended.";
        }
    }

    // 2. Meditate (Prevention Plan Tab)
    const planBanner = document.getElementById('plan-status-banner');
    const planBannerText = document.getElementById('plan-banner-text');
    if (planBanner && planBannerText) {
        planBanner.className = "px-xl py-md flex items-center justify-between text-white transition-colors duration-300";
        if (state.riskTier === 'low') {
            planBanner.classList.add('badge-low');
            planBannerText.textContent = `Low Attention Risk (API: ${state.apiScore.toFixed(2)})`;
        } else if (state.riskTier === 'moderate') {
            planBanner.classList.add('badge-moderate');
            planBannerText.textContent = `Moderate Attention Risk (API: ${state.apiScore.toFixed(2)})`;
        } else {
            planBanner.classList.add('badge-high');
            planBannerText.textContent = `High Attention Risk (API: ${state.apiScore.toFixed(2)})`;
        }
    }
}

// Animate Circular Gauge
function animateDashboardGauge() {
    const circle = document.querySelector('.gauge-ring');
    if (circle) {
        // Circumference is 2 * PI * r = 2 * 3.14159 * 88 = 552.92
        const targetOffset = 552.92 * (1 - state.apiScore);
        circle.style.strokeDashoffset = targetOffset;
        
        // Dynamically color the gauge stroke based on risk tier
        circle.classList.remove('text-commerce-cobalt', 'text-warning-yellow', 'text-critical-red');
        if (state.riskTier === 'low') {
            circle.style.color = '#31A24C';
        } else if (state.riskTier === 'moderate') {
            circle.style.color = '#F2A918';
        } else {
            circle.style.color = '#E41E3F';
        }
    }
}

// Insights Tab: Intensity Bars Animation
function animateInsightsBars() {
    const s = state.signals;
    
    // Width percentages
    const wSession = Math.round((s.sessionDuration / 8.0) * 100);
    const wScroll = Math.round((s.scrollVelocity / 250.0) * 100);
    const wSwitch = Math.round((s.switchFreq / 20.0) * 100);
    const wNight = Math.round(s.nightRatio * 100);
    
    // Get and update progress bars
    updateProgressBar('insights-bar-session', wSession);
    updateProgressBar('insights-bar-night', wNight);
    
    // Text labels
    document.getElementById('insights-lbl-session').textContent = `${wSession}% Intensity`;
    document.getElementById('insights-val-session').textContent = `${s.sessionDuration.toFixed(1)}h Total`;
    
    document.getElementById('insights-val-scroll-rate').textContent = `${Math.round(s.scrollVelocity)}`;
    
    const skipRateEl = document.getElementById('insights-val-skip-rate');
    skipRateEl.textContent = `${Math.round(s.skipFrequency)}%`;
    if (s.skipFrequency > 55) {
        skipRateEl.className = "font-heading-sm text-heading-sm font-bold text-critical-red";
    } else {
        skipRateEl.className = "font-heading-sm text-heading-sm font-bold text-on-surface";
    }
    
    document.getElementById('insights-val-switches').textContent = `${s.switchFreq.toFixed(1)}`;
    document.getElementById('insights-lbl-night').textContent = `${wNight}%`;
}

function updateProgressBar(id, width) {
    const bar = document.getElementById(id);
    if (!bar) return;
    bar.style.width = '0%';
    setTimeout(() => {
        bar.style.width = `${width}%`;
        
        // Dynamically color progress bars based on intensity
        bar.style.backgroundColor = '#0064E0'; // default cobalt
        if (width > 65) {
            bar.style.backgroundColor = '#E41E3F'; // Red for heavy load
        } else if (width > 35) {
            bar.style.backgroundColor = '#F2A918'; // Amber for moderate load
        } else {
            bar.style.backgroundColor = '#31A24C'; // Green for light load
        }
    }, 150);
}

// Insights Tab: SVG Chart drawing
function drawInsightsChart() {
    const svg = document.getElementById('insights-svg');
    if (!svg) return;
    
    // Generate simulated points based on current API Score as a scale factor
    const scaleFactor = state.apiScore / 0.52; // baseline is 0.52
    
    const points = mockChartPoints.map(p => {
        return Math.min(0.95, Math.max(0.05, p.score * scaleFactor));
    });
    
    // SVG coordinates mapping: width 400, height 150.
    // X goes from 20 to 380
    // Y goes from 140 (score = 0.0) to 20 (score = 1.0)
    const computeY = (scoreVal) => 140 - (scoreVal * 120);
    const xStep = 360 / (points.length - 1);
    
    let pathD = `M 20 ${computeY(points[0])}`;
    let areaD = `M 20 140 L 20 ${computeY(points[0])}`;
    
    for (let i = 1; i < points.length; i++) {
        const x = 20 + i * xStep;
        const y = computeY(points[i]);
        
        // Curve construction using bezier
        const prevX = 20 + (i - 1) * xStep;
        const prevY = computeY(points[i - 1]);
        const cpX1 = prevX + xStep / 2;
        const cpY1 = prevY;
        const cpX2 = prevX + xStep / 2;
        const cpY2 = y;
        
        pathD += ` C ${cpX1} ${cpY1}, ${cpX2} ${cpY2}, ${x} ${y}`;
        areaD += ` C ${cpX1} ${cpY1}, ${cpX2} ${cpY2}, ${x} ${y}`;
    }
    
    areaD += ` L 380 140 Z`;
    
    // Update lines in DOM
    const linePath = document.getElementById('insights-chart-line');
    const areaPath = document.getElementById('insights-chart-area');
    const highlightDot = document.getElementById('insights-chart-dot');
    const highlightRing = document.getElementById('insights-chart-ring');
    const anomalyBadge = document.getElementById('insights-anomaly-badge');
    
    if (linePath) {
        linePath.setAttribute('d', pathD);
        // Force redraw animation
        linePath.style.animation = 'none';
        linePath.offsetHeight; /* trigger reflow */
        linePath.style.animation = null;
        
        // Dynamically color graph line based on API risk tier
        if (state.riskTier === 'low') {
            linePath.setAttribute('stroke', '#31A24C');
        } else if (state.riskTier === 'moderate') {
            linePath.setAttribute('stroke', '#F2A918');
        } else {
            linePath.setAttribute('stroke', '#E41E3F');
        }
    }
    
    if (areaPath) {
        areaPath.setAttribute('d', areaD);
        // Update gradient stop colors dynamically
        const stop1 = document.getElementById('grad-stop-1');
        const stop2 = document.getElementById('grad-stop-2');
        if (stop1 && stop2) {
            let color = '#0064E0';
            if (state.riskTier === 'low') color = '#31A24C';
            else if (state.riskTier === 'moderate') color = '#F2A918';
            else color = '#E41E3F';
            
            stop1.style.stopColor = color;
            stop2.style.stopColor = color;
        }
    }
    
    // Move anomaly badge and dot to the maximum point
    const maxVal = Math.max(...points);
    const maxIndex = points.indexOf(maxVal);
    const maxX = 20 + maxIndex * xStep;
    const maxY = computeY(maxVal);
    
    if (highlightDot) {
        highlightDot.setAttribute('cx', maxX);
        highlightDot.setAttribute('cy', maxY);
        if (state.riskTier === 'low') highlightDot.setAttribute('fill', '#31A24C');
        else if (state.riskTier === 'moderate') highlightDot.setAttribute('fill', '#F2A918');
        else highlightDot.setAttribute('fill', '#E41E3F');
    }
    
    if (highlightRing) {
        highlightRing.setAttribute('cx', maxX);
        highlightRing.setAttribute('cy', maxY);
        if (state.riskTier === 'low') highlightRing.setAttribute('stroke', '#31A24C');
        else if (state.riskTier === 'moderate') highlightRing.setAttribute('stroke', '#F2A918');
        else highlightRing.setAttribute('stroke', '#E41E3F');
    }
    
    if (anomalyBadge) {
        // Set dynamic positions
        anomalyBadge.style.left = `${Math.min(270, Math.max(10, maxX - 50))}px`;
        anomalyBadge.style.top = `${Math.max(10, maxY - 30)}px`;
        
        if (state.riskTier === 'high') {
            anomalyBadge.style.display = 'block';
            anomalyBadge.className = 'absolute bg-critical-red text-white text-[10px] px-xs py-xxs rounded shadow-sm font-bold animate-pulse';
            anomalyBadge.textContent = 'Anomaly Detected';
        } else if (state.riskTier === 'moderate') {
            anomalyBadge.style.display = 'block';
            anomalyBadge.className = 'absolute bg-warning-yellow text-white text-[10px] px-xs py-xxs rounded shadow-sm font-bold';
            anomalyBadge.textContent = 'Nudge Alert';
        } else {
            anomalyBadge.style.display = 'none';
        }
    }
}

// Alert History Tab: Render list of alerts
function renderAlertHistory() {
    const listContainer = document.getElementById('alerts-list-container');
    if (!listContainer) return;
    
    if (state.alerts.length === 0) {
        listContainer.innerHTML = `
            <div class="text-center py-section text-secondary font-body-sm">
                No alerts logged yet. Passive monitoring running smoothly.
            </div>
        `;
        return;
    }
    
    listContainer.innerHTML = state.alerts.map(a => {
        let badgeClass = "badge-low";
        let tierLabel = "Low Risk";
        let iconName = "check_circle";
        let iconColor = "text-[#31A24C]";
        
        if (a.riskTier === 'moderate') {
            badgeClass = "badge-moderate";
            tierLabel = "Moderate Risk";
            iconName = "warning";
            iconColor = "text-[#F2A918]";
        } else if (a.riskTier === 'high') {
            badgeClass = "badge-high";
            tierLabel = "High Risk";
            iconName = "error";
            iconColor = "text-[#E41E3F]";
        }
        
        return `
            <div class="p-base border border-hairline-soft rounded-xl bg-white flex flex-col gap-sm">
                <div class="flex justify-between items-start">
                    <span class="font-caption text-caption text-secondary">${a.timestamp}</span>
                    <span class="${badgeClass} text-white font-button-md text-[10px] px-sm py-[2px] rounded-full uppercase tracking-wider">
                        API: ${a.apiScore.toFixed(2)}
                    </span>
                </div>
                <div class="flex gap-md items-center">
                    <span class="material-symbols-outlined ${iconColor} text-[28px] shrink-0" style="font-variation-settings: 'FILL' 1;">${iconName}</span>
                    <div>
                        <h3 class="font-body-md-bold text-body-md-bold text-on-surface">${a.title}</h3>
                        <p class="font-body-sm text-body-sm text-on-surface-variant mt-xxs">${a.description}</p>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}
