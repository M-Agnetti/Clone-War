const ARTEFACTS = 'http://localhost:8080/artefacts';

class APIService {
    getArtefacts(){
        return fetch(ARTEFACTS,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        })
            .then(res => res.json());
    }

}

export default new APIService();