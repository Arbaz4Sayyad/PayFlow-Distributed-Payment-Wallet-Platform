import React from 'react';
import { LandingNavbar } from './LandingNavbar';
import { HeroSection } from './HeroSection';
import { InteractiveWorkbench } from './InteractiveWorkbench';
import { DeveloperExperience } from './DeveloperExperience';
import { ArchitectureGuarantees } from './ArchitectureGuarantees';
import { SecuritySection } from './SecuritySection';
import { FAQSection } from './FAQSection';
import { FinalCTA } from './FinalCTA';
import { LandingFooter } from './LandingFooter';

export const LandingPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-bg-app text-txt-primary flex flex-col font-sans selection:bg-slate-900 selection:text-white">
      <LandingNavbar />
      <main className="flex-grow">
        <HeroSection />
        <InteractiveWorkbench />
        <DeveloperExperience />
        <ArchitectureGuarantees />
        <SecuritySection />
        <FAQSection />
        <FinalCTA />
      </main>
      <LandingFooter />
    </div>
  );
};

export default LandingPage;
