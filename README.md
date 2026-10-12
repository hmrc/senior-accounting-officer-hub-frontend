
# senior-accounting-officer-hub-frontend

This frontend allows customers with an SAO (Senior Accounting Officer) enrolment to begin their submission journey(s).

## How to use the service

Start dependent services using service-manager

`sm2 --start SAO_ALL`

To run the service locally, stop the service manager instance using

`sm2 --stop SENIOR_ACCOUNTING_OFFICER_HUB_FRONTEND`

Run the frontend locally using

`sbt 'run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

Goto http://localhost:10056/senior-accounting-officer/. The actual submission journeys are on `SENIOR_ACCOUNTING_OFFICER_SUBMISSION_FRONTEND` on http://localhost:10058/senior-accounting-officer/submission.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").