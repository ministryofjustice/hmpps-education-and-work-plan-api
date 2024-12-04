# CIAG KPIs
From April 2025 CIAGs will be governed by new commercial contracts between them and MoJ/HMPPS. These contracts are
managed and supported by Curious.

An aspect of the new contracts covers prisoner education where the CIAGs will be measured against Key Performance
Indicators (KPIs). The prisoner's Induction and Goals, and subsequent reviews as delivered by PLP functionality is
considered part of the prisoner's education.  

The KPIs and associated reporting is a function of Curious, but the PLP API needs to integrate with Curious and vice 
versa at various points in order to provide the necessary data to Curious. There are 2 KPIs that impact and are of 
relevance to PLP.

The prisoner education aspects of the CIAG contracts are provided by Curious functionality.  
The **Prisoner Education Framework (PEF)** is part of Curious 1 and does not include the required data and functionality 
to support the KPIs.  
Curious 2 will replace PEF with **Prison Education Service (PES)** which will include the required data and 
functionality to support the KPIs.  

At time of writing (20/09/24) the CIAG contracts cover prisoner education via PEF, and is supported by Curious 1. When 
Curious 2 is released prisoner education will be addressed with PES.

The documentation and diagrams in this folder describe the integration flow / sequences for the 2 KPIs.

## Mermaid diagrams
Files in this folder with the extension `.mermaid` are Mermaid Charts.  
You can either install the intellij plugin to be able to edit and view the resultant charts in your IDE, or you can 
copy and paste the file content into the [online Mermaid editor](https://www.mermaidchart.com/).

**Please note** - In the sequence diagrams coloured boxes are used to group together and designate higher level
processes. Without the boxes it is hard to see where one process ends and another starts.
* Blue boxes represent PLP based processing
* Green boxes represent the HMPPS Integration API
* Red boxes represent Curious based processes

## CIAG KPI 1
KPI 1 is about measuring the CIAGs performance in respect of completing Inductions on time.

### PEF vs PES
The new CIAG contracts technically start from April 2025, but Curious 2 won't go live until October 2025, so between 
April 2025 and October 2025, even though technically the CIAGs are under the new contracts, not all of it can be 
supported because Curious 1 is still being used. IE. PEF will still be in place which does not support the Curious data 
structures and functionality required for KPI 1.

Both PLP and Curious need to support KPI 1 from April 25, but the CIAGs won't be financially penalised for not meeting 
the contracts.  
Specifically for PLP, as per the new contract wording, the CIAGs have 10 days after completing **all relevant** screening
and assessments in order to do the Induction.  
But PEF / Curious 1 (April 2025 till October 2025) cannot determine **all** and **relevant**, so cannot send PLP
the necessary triggers or data. 
PES / Curious 2 will be able to do that.

As an interim it has been agreed with the stakeholders that between April 2025 and October 2025 (PEF / Curious 1) PLP 
will determine the due date of the Induction based on the date the prisoner entered prison + 30 days (current thinking, 
but TBC); without any regard for when screenings and assessments might or might not have been done in Curious.

This folder contains 2 sequence diagrams that cover KPI 1, one for PEF April 2025 to October 2025, and one for
PES October 2025 onwards.

## CIAG KPI 2
KPI 2 is about measuring the CIAGs performance in respect of completing Reviews on time.

