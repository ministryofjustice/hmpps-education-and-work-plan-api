# Tests specifically for the CIAG KPI PEF ruleset
The tests in this package are specifically for testing the behaviour of the CIAG KPI PEF ruleset.  
The PEF ruleset is currently enabled, and will be disabled on 01/09/2025, at which point the PES rules apply.

PES will be enabled on 01/09/2026, after which point all tests in this package can be removed, along with
`application-ciag-kpi-ref-rules.yml` and all branching logic/bean implementation based on the feature
toggle/property `ciag-kpi-processing-rule`.
