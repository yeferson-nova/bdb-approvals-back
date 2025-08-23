CREATE TABLE approval_requests(
                                  id UUID PRIMARY KEY,
                                  title VARCHAR(160) NOT NULL,
                                  description TEXT,
                                  requester_upn VARCHAR(320) NOT NULL,
                                  approver_upn VARCHAR(320) NOT NULL,
                                  type VARCHAR(60) NOT NULL,
                                  status VARCHAR(20) NOT NULL,
                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE approval_actions(
                                 id UUID PRIMARY KEY,
                                 request_id UUID NOT NULL REFERENCES approval_requests(id) ON DELETE CASCADE,
                                 actor_upn VARCHAR(320) NOT NULL,
                                 action VARCHAR(20) NOT NULL,
                                 comment TEXT,
                                 occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_req_status ON approval_requests(status);
CREATE INDEX idx_req_approver ON approval_requests(approver_upn);
CREATE INDEX idx_req_created_at ON approval_requests(created_at);
