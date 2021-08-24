# FIDO UAF Server API

- Registration
   - [Registration Request via Username](#request-via-username) - `/fido/v1/registration/request/{username:.+}`
   - [Registration Request via Username and appid](#request-via-username-and-appid) - `/fido/v1/registration/request/{username:.+}/{appid}`
   - [Registration Response](#registration-response) - `/fido/v1/registration/response`
- Deregistration
   - [Deregistration](#deregistration) - `/fido/v1/registration/dereg/{username:.+}` :warning:
- Authentication
   - [Authentication Request](#request) - `/fido/v1/authentication/request`
   - [Authentication Request via appid](#request-via-appid) - `/fido/v1/authentication/request/{appid}`
   - [Authentication Request via appid and trxcontent](#request-via-appid-and-trxcontent) - `/fido/v1/authentication/request/{appid}/{trxcontent}`
   - [Authentication Request via username and trxcontent](#request-via-username-and-trxcontent) - `/fido/v1/authentication/request/trx/{username:.+}/{trxcontent}`
   - [Authentication Response](#authentication-response) - `/fido/v1/authentication/response`
- Other
   - [Recovery](#recovery) - `/fido/v1/recovery/dereg/{username:.+}` :warning:
   - [About](#about) - `/fido/v1/about`
   - [Statistics](#statistics) - `/fido/v1/stats` :x:
   - [TrustedFacets](#trustedfacets) - `/fido/v1/trustedfacets`
   - [PopulateTrustedFacets](#populatetrustedfacets)- `/fido/v1/populatetrustedfacets` :x:
   - [Registerfacet](#registerfacet) - `/fido/v1/registerfacet/{facet:.+}/{description:}` :x:
   - [Isauthenticated](#isauthenticated) - `/fido/v1/isauth/{auth}`
   - [Last Authentication](#last-authentication) - `/fido/v1/lastauth/{username:.+}` :warning:
   - [Logout](#logout) - `/fido/v1/logout/{username:.+}` :x:


## Registration

### Request via Username

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/registration/request/{username:.+} |
| **Description** | Request to initialize the registration process |
| **Status** | :heavy_check_mark: Tested. Works with client app |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Reg",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "serverData": "ZkVLYUN2OHhneklvaG82b3Jvek92S2hpUm8tUTJaVVQ5U0pZM1R1QnV0NC5NVFl4TlRnek9EazROREUzTUEuZEdWemRBLlNrUkthRXBFUlhkS1JrNU5VbFYwZGs1VVpIcGFXR1J2VTFWb1JGZFZPVXRTVjFaRFpGaFY"
    },
    "challenge": "JDJhJDEwJFNMRUtvNTdzZXdoSUhDWU9KRWVCdXU",
    "username": "test",
    "policy": {
      "accepted": [
        [{"aaid": ["001D#0002"]}],
        [{"aaid": ["0045#0005"]}],
        [{"aaid": ["0047#0002"]}],
        [{"aaid": ["004A#2200"]}],
        [{"aaid": ["004A#2300"]}],
        [{"aaid": ["0053#0001"]}],
        [{"aaid": ["0053#0002"]}],
        [{"aaid": ["9874#0001"]}],
        [{"aaid": ["9874#0101"]}],
        [{"aaid": ["EBA0#0001"]}]
      ]
    }
  }
]
```

### Request via Username and appid

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/registration/request/{username:.+}/{appid} |
| **Description** | Request to initialize the registration process using username and appid |
| **Status** | :heavy_check_mark: Tested. Works via Browser. |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Reg",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "serverData": "ZG9sUnZaN29PYWctM1lOQmxIZjVfYXNFWHN5ZVNFbXo3WDk5Z2Z6QjV6WS5NVFl4TlRnME1UVXdPVGs0TmcuZEdWemRBLlNrUkthRXBFUlhkS1JYQndUVEZTU2xaV2NEQmxiV2hoVlVWS1RVMHhhek5WTUZadFlsaFY"
    },
    "challenge": "JDJhJDEwJEppM1RJVVp0emhaUEJMM1k3U0VmbXU",
    "username": "test",
    "policy": {
      "accepted": [
        [{"aaid": ["001D#0002"]}],
        [{"aaid": ["0045#0005"]}],
        [{"aaid": ["0047#0002"]}],
        [{"aaid": ["004A#2200"]}],
        [{"aaid": ["004A#2300"]}],
        [{"aaid": ["0053#0001"]}],
        [{"aaid": ["0053#0002"]}],
        [{"aaid": ["9874#0001"]}],
        [{"aaid": ["9874#0101"]}],
        [{"aaid": ["EBA0#0001"]}]
      ]
    }
  }
]
```

### Registration Response

| **Name** | **Value** |
|------|-------|
| **Methods** | POST |
| **Endpoint** | /fido/v1/registration/response |
| **Description** | The server's response to the registration process |
| **Status** | :heavy_check_mark: Tested. Works with client app. |

Example Client Payload
```json
[
  {
    "assertions": [
      {
        "assertion": "AT5KAwM-9wALLgkARUJBMCMwMDAxDi4HAAAAAQIAAQEKLiAAm5f2IInVq-TvXZ6HsAezljGXWXg0\nMxljm6dIZzOkomEJLiwAbjM5d3A0ZHhMclFjTG5pUHktOFEwdG9WVnhQbEdJMWZzbUMyRnU3MGpZ\nRT0NLggAAAAAAAAAAAAMLlsAMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEU37vDyOnSBPw411k\n8PVDuU4yuN902fjZb_t0fQ-3VwdS6sWwUuoUhtq__o4F6p2YfRcu8ihM86UFC5NN7XJfB_w_EABm\nMTVkMDg0YzU3YzYwMDA2_T8IAFNNLUc5NjVGBz5LAgYuRgAwRAIgUOS-5ZAfmZAwz1BPGTh58_wT\n_RShJm_co48WpSDZx6ACIEJz_ihBlEXPUznLAg6TyTzjEooNXTPfzQY5ZeSAdvZHBS79ATCCAfkw\nggGfoAMCAQICBFUxTNMwCQYHKoZIzj0EATCBhDELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAkNBMREw\nDwYDVQQHDAhTYW4gSm9zZTETMBEGA1UECgwKZUJheSwgSW5jLjEMMAoGA1UECwwDVE5TMRIwEAYD\nVQQDDAllQmF5LCBJbmMxHjAcBgkqhkiG9w0BCQEWD25wZXNpY0BlYmF5LmNvbTAeFw0xNTA0MTcx\nODExMzFaFw0xNTA0MjcxODExMzFaMIGEMQswCQYDVQQGEwJVUzELMAkGA1UECAwCQ0ExETAPBgNV\nBAcMCFNhbiBKb3NlMRMwEQYDVQQKDAplQmF5LCBJbmMuMQwwCgYDVQQLDANUTlMxEjAQBgNVBAMM\nCWVCYXksIEluYzEeMBwGCSqGSIb3DQEJARYPbnBlc2ljQGViYXkuY29tMFkwEwYHKoZIzj0CAQYI\nKoZIzj0DAQcDQgAEPIcOZR01F72d0s2PWq4GzjgQ9qZ-cwDOJm0ocEMgi-W0bJEy9x1T1j0MamFT\nPt6SbSSC2KjlrCDHeUW_4fA_hDAJBgcqhkjOPQQBA0kAMEYCIQCKZKKfy93zrZu_UNpSqysXmvin\nLwNYOqpM2o-NlQ9oYAIhAK7T6UxAAyFs9sq5eVxnyVXH3BZr6TALjCr7hNSe946e\n",
        "assertionScheme": "UAFV1TLV"
      }
    ],
    "fcParams": "eyJhcHBJRCI6Imh0dHBzOi8vZmlkb3VhZi5kcy51bmlwaS5nci9maWRvL3YxL3RydXN0ZWRmYWNl\ndHMiLCJjaGFsbGVuZ2UiOiJKREpoSkRFd0pGTk1SVXR2TlRkelpYZG9TVWhEV1U5S1JXVkNkWFUi\nLCJjaGFubmVsQmluZGluZyI6e30sImZhY2V0SUQiOiJhbmRyb2lkOmFway1rZXktaGFzaDp5dTIz\nRjZOMDEwaEhaeUNuWk1aZUVRRGZzaTdoX3JhamhJU3VGYXp6YmRjXHUwMDNkIn0=\n",
    "header": {
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "op": "Reg",
      "serverData": "ZkVLYUN2OHhneklvaG82b3Jvek92S2hpUm8tUTJaVVQ5U0pZM1R1QnV0NC5NVFl4TlRnek9EazROREUzTUEuZEdWemRBLlNrUkthRXBFUlhkS1JrNU5VbFYwZGs1VVpIcGFXR1J2VTFWb1JGZFZPVXRTVjFaRFpGaFY",
      "upv": {"major": 1, "minor": 0}
    }
  }
]
```

Example Server Response
```json
[
  {
    "authenticator": {
      "AAID": "EBA0#0001",
      "KeyID": "bjM5d3A0ZHhMclFjTG5pUHktOFEwdG9WVnhQbEdJMWZzbUMyRnU3MGpZRT0",
      "deviceId": null,
      "username": null,
      "status": null,
      "timestamp": null,
      "authenticationId": null,
      "radiusPassword": null
    },
    "PublicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEU37vDyOnSBPw411k8PVDuU4yuN902fjZb_t0fQ-3VwdS6sWwUuoUhtq__o4F6p2YfRcu8ihM86UFC5NN7XJfBw",
    "SignCounter": "0",
    "AuthenticatorVersion": "0.0",
    "tcDisplayPNGCharacteristics": null,
    "username": "test",
    "userId": null,
    "deviceId": "f15d084c57c60006�?\b\u0000SM-G965F",
    "timeStamp": "1615838986618",
    "status": "1200",
    "attestCert": "MIIB-TCCAZ-gAwIBAgIEVTFM0zAJBgcqhkjOPQQBMIGEMQswCQYDVQQGEwJVUzELMAkGA1UECAwCQ0ExETAPBgNVBAcMCFNhbiBKb3NlMRMwEQYDVQQKDAplQmF5LCBJbmMuMQwwCgYDVQQLDANUTlMxEjAQBgNVBAMMCWVCYXksIEluYzEeMBwGCSqGSIb3DQEJARYPbnBlc2ljQGViYXkuY29tMB4XDTE1MDQxNzE4MTEzMVoXDTE1MDQyNzE4MTEzMVowgYQxCzAJBgNVBAYTAlVTMQswCQYDVQQIDAJDQTERMA8GA1UEBwwIU2FuIEpvc2UxEzARBgNVBAoMCmVCYXksIEluYy4xDDAKBgNVBAsMA1ROUzESMBAGA1UEAwwJZUJheSwgSW5jMR4wHAYJKoZIhvcNAQkBFg9ucGVzaWNAZWJheS5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQ8hw5lHTUXvZ3SzY9argbOOBD2pn5zAM4mbShwQyCL5bRskTL3HVPWPQxqYVM-3pJtJILYqOWsIMd5Rb_h8D-EMAkGByqGSM49BAEDSQAwRgIhAIpkop_L3fOtm79Q2lKrKxea-KcvA1g6qkzaj42VD2hgAiEArtPpTEADIWz2yrl5XGfJVcfcFmvpMAuMKvuE1J73jp4",
    "attestDataToSign": "Az73AAsuCQBFQkEwIzAwMDEOLgcAAAABAgABAQouIACbl_YgidWr5O9dnoewB7OWMZdZeDQzGWObp0hnM6SiYQkuLABuMzl3cDRkeExyUWNMbmlQeS04UTB0b1ZWeFBsR0kxZnNtQzJGdTcwallFPQ0uCAAAAAAAAAAAAAwuWwAwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARTfu8PI6dIE_DjXWTw9UO5TjK433TZ-Nlv-3R9D7dXB1LqxbBS6hSG2r_-jgXqnZh9Fy7yKEzzpQULk03tcl8H_D8QAGYxNWQwODRjNTdjNjAwMDb9PwgAU00tRzk2NUY",
    "attestSignature": "MEQCIFDkvuWQH5mQMM9QTxk4efP8E_0UoSZv3KOPFqUg2cegAiBCc_4oQZRFz1M5ywIOk8k84xKKDV0z380GOWXkgHb2Rw",
    "attestVerifiedStatus": "VALID"
  }
]
```
### Deregistration

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/registration/dereg/{username:.+} |
| **Description** | Request to initialize the deregistration process |
| **Status** | :heavy_check_mark: Tested. Works with client app. |
| **Notes** | :warning: This deregisters the first authenticator it finds on the database. The endpoint is accessible by anyone. This need improvements. |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Dereg",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets"
    },
    "authenticators": [
      {
        "aaid": "EBA0#0001",
        "keyID": "bjM5d3A0ZHhMclFjTG5pUHktOFEwdG9WVnhQbEdJMWZzbUMyRnU3MGpZRT0"
      }
    ]
  }
]
```

## Authentication

### Request

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/authentication/request |
| **Description** | Request to initialize the authentication process |
| **Status** | :heavy_check_mark: Tested. Works with client app. |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Auth",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "serverData": "c3JtWktqZ1U1UUtvVzA0aUU3bU8zeXNrWElqM1hfQ3hxX1UwTUhJR2VUOC5NVFl4TlRnek9EazVNRGszTncuU2tSS2FFcEVSWGRLUjJSYVZVTTFVRkpXUmt0TWJWbDJaRlphUjFwdGN6QmtWRnB4VkhrMA"
    },
    "challenge": "JDJhJDEwJGdZUC5PRVFKLmYvdVZGZms0dTZqTy4",
    "policy": {
      "accepted": [
        [{"aaid": ["001D#0002"]}],
        [{"aaid": ["0045#0005"]}],
        [{"aaid": ["0047#0002"]}],
        [{"aaid": ["004A#2200"]}],
        [{"aaid": ["004A#2300"]}],
        [{"aaid": ["0053#0001"]}],
        [{"aaid": ["0053#0002"]}],
        [{"aaid": ["9874#0001"]}],
        [{"aaid": ["9874#0101"]}],
        [{"aaid": ["EBA0#0001"]}]
      ]
    }
  }
]
```

### Request via appid

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/authentication/request/{appid} |
| **Description** | Request to initialize the authentication process using appid |
| **Status** | :heavy_check_mark: Tested. Works via browser. |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Auth",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "serverData": "UmVhdU5TNVNPZDJ4MG5EMVNzcFc3cmI5WUM2ZEZfZ2RzTFJzcE1qUFNyRS5NVFl4TlRnME56VXdPRGMwT0EuU2tSS2FFcEVSWGRLUlRWeVlrTTVRbFJFVm1oWFZscEZVMWhOZWxGcVdYaGpNMUo2VkZoVg"
    },
    "challenge": "JDJhJDEwJE5rbC9BTDVhWVZESXMzQjYxc3RzTXU",
    "transaction": null,
    "policy": {
      "accepted": [
        [{"aaid": ["001D#0002"]}],
        [{"aaid": ["0045#0005"]}],
        [{"aaid": ["0047#0002"]}],
        [{"aaid": ["004A#2200"]}],
        [{"aaid": ["004A#2300"]}],
        [{"aaid": ["0053#0001"]}],
        [{"aaid": ["0053#0002"]}],
        [{"aaid": ["9874#0001"]}],
        [{"aaid": ["9874#0101"]}],
        [{"aaid": ["EBA0#0001"]}]
      ]
    }
  }
]
```

### Request via appid and trxcontent

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/authentication/request/{appid}/{trxcontent} |
| **Description** | Request to initialize the authentication process using appid and trxcontent |
| **Status** | :heavy_check_mark: Tested. Works via browser. |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Auth",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "serverData": "UkZaaWJYZDNpUjkwQ1ZUdEpwZjBkQUg1bllkWHNuZXhJMm1CajlCS19Vay5NVFl4TlRnME56WXhOVGM0TXcuU2tSS2FFcEVSWGRLUkdjeVYyNWFXbEV6U2tKa01WcHFXVEZyTlZWWVZUUmhla3BhWTJzNA"
    },
    "challenge": "JDJhJDEwJDg2WnZZQ3JBd1ZjY1k5UXU4azJZck8",
    "transaction": null,
    "policy": {
      "accepted": [
        [{"aaid": ["001D#0002"]}],
        [{"aaid": ["0045#0005"]}],
        [{"aaid": ["0047#0002"]}],
        [{"aaid": ["004A#2200"]}],
        [{"aaid": ["004A#2300"]}],
        [{"aaid": ["0053#0001"]}],
        [{"aaid": ["0053#0002"]}],
        [{"aaid": ["9874#0001"]}],
        [{"aaid": ["9874#0101"]}],
        [{"aaid": ["EBA0#0001"]}]
      ]
    }
  }
]
```

### Request via username and trxcontent

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/authentication/request/trx/{username:.+}/{trxcontent} |
| **Description** | Request to initialize the authentication process using username and trxcontent |
| **Status** | :heavy_check_mark: Tested. Works via browser. |

Example Server Response
```json
[
  {
    "header": {
      "upv": {"major": 1, "minor": 0},
      "op": "Auth",
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "serverData": "Y0V4WnZmT2ltNDhhY2FYTXVoYS1VYTdPemJuS21xTFZ1R0ZjY21rR0tBMC5NVFl4TlRnME56ZzRNelEwTmcuU2tSS2FFcEVSWGRLUlc4MVRsY3hhRlZHU1RCaGFrSTJUMFJrYWxKc2FFaGxhbEoxVFRBNA"
    },
    "challenge": "JDJhJDEwJEo5NW1hUFI0ajB6ODdjRlhHejRuM08",
    "transaction": null,
    "policy": {
      "accepted": [
        [{"aaid": ["001D#0002"]}],
        [{"aaid": ["0045#0005"]}],
        [{"aaid": ["0047#0002"]}],
        [{"aaid": ["004A#2200"]}],
        [{"aaid": ["004A#2300"]}],
        [{"aaid": ["0053#0001"]}],
        [{"aaid": ["0053#0002"]}],
        [{"aaid": ["9874#0001"]}],
        [{"aaid": ["9874#0101"]}],
        [{"aaid": ["EBA0#0001"]}]
      ]
    }
  }
]
```

### Authentication Response

| **Name** | **Value** |
|------|-------|
| **Methods** | POST |
| **Endpoint** | /fido/v1/authentication/response |
| **Description** | The server's response to the registration process |
| **Status** | :heavy_check_mark: Tested. Works with client app. |

Example Client Payload
```json
[
  {
    "assertions": [
      {
        "assertion": "Aj7SAAQ-ggALLgkARUJBMCMwMDAxDi4FAAAAAQIADy4IAISLYGVofyePCi4gAGGy_ox5MqqFrdzy\ncJ6mNL0VDv36XvUjSw_cAVn5m85iEC4AAAkuLABuMzl3cDRkeExyUWNMbmlQeS04UTB0b1ZWeFBs\nR0kxZnNtQzJGdTcwallFPQ0uBAAAAAAABi5IADBGAiEA5nfMPhH9BEjkwBZx2-7xoj2ButNa-vvQ\n0kWkp45EgFcCIQDBT7IuB5SOTnCP7tTM4KFVzsLiFRB20u8o_RKYH2hpsA==\n",
        "assertionScheme": "UAFV1TLV"
      }
    ],
    "fcParams": "eyJhcHBJRCI6Imh0dHBzOi8vZmlkb3VhZi5kcy51bmlwaS5nci9maWRvL3YxL3RydXN0ZWRmYWNl\ndHMiLCJjaGFsbGVuZ2UiOiJKREpoSkRFd0pHZFpVQzVQUlZGS0xtWXZkVlpHWm1zMGRUWnFUeTQi\nLCJjaGFubmVsQmluZGluZyI6e30sImZhY2V0SUQiOiJhbmRyb2lkOmFway1rZXktaGFzaDp5dTIz\nRjZOMDEwaEhaeUNuWk1aZUVRRGZzaTdoX3JhamhJU3VGYXp6YmRjXHUwMDNkIn0=\n",
    "header": {
      "appID": "https://fidouaf.ds.unipi.gr/fido/v1/trustedfacets",
      "op": "Auth",
      "serverData": "c3JtWktqZ1U1UUtvVzA0aUU3bU8zeXNrWElqM1hfQ3hxX1UwTUhJR2VUOC5NVFl4TlRnek9EazVNRGszTncuU2tSS2FFcEVSWGRLUjJSYVZVTTFVRkpXUmt0TWJWbDJaRlphUjFwdGN6QmtWRnB4VkhrMA",
      "upv": {"major": 1, "minor": 0}
    }
  }
]
```

Example Server Response
```json
[
  {
    "AAID": "EBA0#0001",
    "KeyID": "bjM5d3A0ZHhMclFjTG5pUHktOFEwdG9WVnhQbEdJMWZzbUMyRnU3MGpZRT0",
    "deviceId": "f15d084c57c60006�?\b\u0000SM-G965F",
    "username": "test",
    "status": "1200",
    "timestamp": "1615838992827",
    "authenticationId": "fido_auth_id_jbIpklvw3_4CZjCJ",
    "radiusPassword": "8p2GDs_X9JDB4lBbOw95f4W2QVk"
  }
]
```

## Recovery

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/recovery/dereg/{username:.+} |
| **Description** | Determines whether or not a user has keys |
| **Status** | :heavy_check_mark: Tested. Works via browser. |
| **Notes** | :warning: There is no need for this endpoint to be public. |

Example Server Response
```json
{
  "code": 1,
  "reason": "User has 1 keys!"
}
```

Example Server Response
```json
{
  "code": 1,
  "reason": "User has 0 keys!"
}
``` 

## About

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/about |
| **Description** | Return Server's informations |
| **Status** | :heavy_check_mark: Tested. Works via browser. |

Example Server Response
```json
{
  "about": {
    "Archiver-Version": "Plexus Archiver",
    "Implementation-Vendor": "University of Piraeus - Systems Security Laboratory",
    "Implementation-Title": "FIDO UAF Server",
    "Implementation-Build": "20210314010447",
    "Implementation-Version": "1.0.1-SNAPSHOT",
    "Manifest-Version": "1.0",
    "Created-By": "Apache Maven 3.6.3",
    "Implementation-Vendor-Id": "eu.unipi",
    "Built-By": "fido-server",
    "Build-Jdk": "1.8.0_271",
    "Implementation-Branch": ""
  }
}
```

## Controller

### Statistics

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/stats |
| **Description** | Return Server's statistics |
| **Status** | :x: Tested. Doesn't work. |

### TrustedFacets

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/trustedfacets |
| **Description** | Return Server's TrustedFacets |
| **Status** | :heavy_check_mark: Tested. Works with client app. |

Example Server Response
```json
{
  "trustedFacets": [
    {
      "version": {
        "major": 1,
        "minor": 0
      },
      "ids": [
        "android:apk-key-hash:aMkHGqtM4VcxPUyycda6AWJzkRoBkBm2eyopphujcMk=",
        "ios:bundle-id:org.fidoalliance.ios.conformance",
        "ios:bundle-id:com.pramati.fido",
        "android:apk-key-hash:STYk2VhgMlPqdF8xR7vdOqxpzx8",
        "android:apk-key-hash:SvYZ4Sgas9T2+6DpNj566iscuns",
        "android:apk-key-hash:mCvIumI5e2a15oYIdlDJvkekzvA",
        "ios:bundle-id:io.xtrong.fidocombo",
        "android:apk-key-hash:IF8bLNAHrWtPn8vX33kGi-BNkmjkMA4CopjBGi0A0KY=",
        "android:apk-key-hash:NWY4hg5ACDXLkHP1DJVBgjUB71U",
        "android:apk-key-hash:kIxpVBGf4ni6lQfLGB2L94003Jc",
        "ios:bundle-id:com.pramati.fidoclient",
        "ios:bundle-id:com.pramati.FPAuth.RPApp",
        "android:apk-key-hash:I3xzJRPGJ7EUKRmyBIkjoGOUY2c",
        "android:apk-key-hash:aLxLnh1pGt7wo_KJcvOnS6eEZUNkt1sQyOkLvwbL20s=",
        "android:apk-key-hash:XXyC7sBtJh0NTUX42DYj0mKGDMipFKoa8Dw9mlLDjwg=",
        "android:apk-key-hash:73s46+W07WTzi5C8NQ96cXui5+U",
        "android:apk-key-hash:pRMaihBun2mCa-Tjxc42Cw0U_cPlP_W5XjH7tLTweZI=",
        "android:apk-key-hash:RZ-lYlABm5muGbRYsbR-y0WKqOpV1YVa8yLVGNsC_ys=",
        "android:apk-key-hash:D1p2Wjm9acNBIrs2uplUzKesowP-h_HfUZWub0tPTHA=",
        "android:apk-key-hash:WgnwdKFfOqH4MAqEh33K6D_7JRtBpq5g4chkDvwbgzU=",
        "android:apk-key-hash:yu23F6N010hHZyCnZMZeEQDfsi7h_rajhISuFazzbdc=",
        "android:apk-key-hash:oiv39OwTUWyGw6KuD-nE7R6mOnlt_rj0NUfZt1xxFXA=",
        "https://www.head2toes.org",
        "https://openidconnect.ebay.com",
        "android:apk-key-hash:Df+2X53Z0UscvUu6obxC3rIfFyk",
        "android:apk-key-hash:bE0f1WtRJrZv/C0y9CM73bAUqiI",
        "android:apk-key-hash:Lir5oIjf552K/XN4bTul0VS3GfM",
        "android:apk-key-hash:MCbbE5mV14rEvgk99uN6qN/MNCI",
        "android:apk-key-hash:RVimey8gA1qIjM9FAc5yCjAbZcQ"
      ]
    }
  ]
}

```

### PopulateTrustedFacets

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/populatetrustedfacets |
| **Description** | Return Populate trustedFacets |
| **Status** | :x: Tested. Doesn't work. |

### Registerfacet

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/registerfacet/{facet:.+}/{description:} |
| **Description** | Returns whether a trustedfacet is registered or not |
| **Status** | :x: Tested. Doesn't work. |

Example Server Response
```json
could not execute statement
```

### Isauthenticated

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/isauth/{auth} |
| **Description** | Return if a user is authenticated or no |
| **Status** | :heavy_check_mark: Tested. Works via browser. |


Example Server Response
```json
{
  "authenticated": true,
  "username": "test",
  "timestamp": "1615838992827"
}
```

### Last Authentication

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/lastauth/{username:.+} |
| **Description** | Return the last timestamp of a client's authentication |
| **Status** | Tested not sure that works properly, it returns the client's registration timestamp. |
| **Notes** | :warning: There is no need for this endpoint to be public. |

Example Server Response
```json
{
  "username_id": "test",
  "last_auth_timestamp": "1615848874486"
}
```

### Logout

| **Name** | **Value** |
|------|-------|
| **Methods** | GET |
| **Endpoint** | /fido/v1/logout/{username:.+} |
| **Description** | Return logout information |
| **Status** | :x: Tested. Doesn't work. |


